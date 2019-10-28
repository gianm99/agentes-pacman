/*
Tomás Bordoy García-Carpintero
Gian Lucas Martín Chamorro
 */
package agents;

public class Bitxo22 extends Agent {

    static final boolean DEBUG = false;

    static final int PARET = 0;
    static final int NAU = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    Estat estat;
    int espera = 0;

    long temps;
    int tempsColisio;  //Temps que duu el bitxo en col·lisió
    int anteriorImpactes;   //Impactes rebuts a l'anterior avaluaComportament()
    int tempsEvasio;    //Temps que duu en mode "evasió"
    boolean enEvasio = false;   //Es troba en mode "evasió"

    static final int ANGLE_VISORS = 40;
    static final int DISTANCIA_VISORS = 300;
    static final int VELOCITAT_LINEAL_NORMAL = 4;
    static final int VELOCITAT_LINEAL_COMBAT = 6;
    static final int VELOCITAT_ANGULAR = 9;

    public Bitxo22(Agents pare) {
        super(pare, "Chop", "imatges/chop.gif");
    }

    @Override
    public void inicia() {
        posaAngleVisors(ANGLE_VISORS);        // 0 - 70
        posaDistanciaVisors(DISTANCIA_VISORS);   // 0 - 400
        posaVelocitatLineal(VELOCITAT_LINEAL_NORMAL);     // 1 - 6
        posaVelocitatAngular(VELOCITAT_ANGULAR);    // 1 - 9
        espera = 0;
        temps = 0;
        tempsColisio = 0;
        tempsEvasio = 0;
    }

    @Override
    public void avaluaComportament() {
        temps++;
        // guarda el nombre d'impactes rebuts de l'anterior estat
        if (estat != null) {
            anteriorImpactes = estat.impactesRebuts;
        }
        estat = estatCombat();
        if (espera > 0) {
            espera--;
        } else {
            atura();

            if (estat.enCollisio) // situació de nau bloquejada
            {
                //
                tempsColisio++;
                // si veu una nau, dispara
                if (estat.objecteVisor[CENTRAL] == NAU && estat.bales > 0) {
                    dispara();   //bloqueig per nau, no giris dispara
                    tempsColisio = 0;
                } else if (estat.objecteVisor[CENTRAL] == NAU && estat.bales == 0 && estat.hyperespaiDisponibles > 0) {
                    hyperespai();   //bloqueig per nau i no té bales
                    tempsColisio = 0;
                } else {
                    if (tempsColisio < 10) {
                        //primer intenta girar a l'esquerra
                        gira(40); // 40 graus
                    } else {
                        //si no funciona, prova girant a la dreta
                        gira(-40);
                    }
                    if (hiHaParedDavant(15)) {
                        //si hi ha una paret davant, prova anant enrere
                        enrere();
                        espera = 3;
                    } else {
                        //si no, prova anant endavant
                        endavant();
                    }
                }
                if (tempsColisio > 20) {
                    //si duu en col·lisió molt de temps, fa hyperespai
                    hyperespai();
                    tempsColisio = 0;
                }
            } else {
                tempsColisio = 0; //posa a 0 el comptador de temps en col·lisió
                endavant();
                ObjecteMesProper(); //cerca l'objecte més proper
                if (estat.objecteVisor[CENTRAL] == NAU && !estat.disparant) {
                    dispara();
                }
                evasio(); //activa el mode evasió si l'estan disparant
                int sensor = 0; //mira els visors per detectar obstacles
                if ((estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < 45)) {
                    sensor += 1;
                }
                if ((estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < 45)) {
                    sensor += 2;
                }
                if ((estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < 45)) {
                    sensor += 4;
                }
                switch (sensor) {
                    case 0:
                        endavant();
                        break;
                    case 1:
                    case 3: // esquerra bloquejada
                        dreta();
                        break;
                    case 4:
                    case 6: // dreta bloquejada
                        esquerra();
                        break;
                    case 5:
                        endavant();
                        break; // centre lliure
                    case 2: // paret davant
                    case 7: // si estic molt aprop, torna enrere
                        double distancia;
                        distancia = minimaDistanciaVisors();

                        if (distancia < 15) {
                            gira(90);
                            enrere();
                            espera = 2;
                        } else {
                            switch (paretMesPropera()) {
                                case ESQUERRA:
                                    dreta();
                                case DRETA:
                                    esquerra();
                            }
                        }
                        break;
                }
                esquivarMina(); //esquiva les mines
            }

        }
    }

    /**
     * Detecta si hi ha parets davant
     *
     * @param dist distància a la que es vol detectar les parets
     * @return true si hi ha una paret a menys de la distància dist
     */
    boolean hiHaParedDavant(int dist) {

        if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] <= dist) {
            return true;
        }

        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] <= dist) {
            return true;
        }

        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] <= dist) {
            return true;
        }

        return false;
    }

    /**
     * Cerca l'objecte (enemic o recurs) més proper i gira cap a ell amb el
     * mètode mira()
     */
    public void ObjecteMesProper() {
        if (closestRecurs() != null && closestEnemic() != null) { //enemic i recurs
            posaVelocitatLineal(VELOCITAT_LINEAL_COMBAT);
            if (closestEnemic().agafaDistancia() <= closestRecurs().agafaDistancia() && estat.bales > 0) {
                mira(closestEnemic());
            } else {
                mira(closestRecurs());
            }
        } else if (closestRecurs() != null && closestEnemic() == null) { //enemic
            mira(closestRecurs());
            posaVelocitatLineal(VELOCITAT_LINEAL_COMBAT);
        } else if (closestRecurs() == null && closestEnemic() != null) { //recurs
            posaVelocitatLineal(VELOCITAT_LINEAL_COMBAT);
            if (estat.bales > 0) {
                mira(closestEnemic());
            }
        } else { //ni enemic ni recurs
            posaVelocitatLineal(VELOCITAT_LINEAL_NORMAL);
        }

    }

    /**
     * Cerca el recurs més proper i retorna l'objecte.
     *
     * @return Objecte del recurs més proper o null.
     */
    public Objecte closestRecurs() {
        Objecte closestRecurs;
        closestRecurs = estat.recurs[0];

        for (int i = 0; i < estat.numRecursos; i++) {
            if (estat.recurs[i].agafaDistancia() < closestRecurs.agafaDistancia()) {
                closestRecurs = estat.recurs[i];
            }

        }

        return closestRecurs;
    }

    /**
     * Cerca l'enemic més proper i retorna l'objecte.
     *
     * @return Objecte de l'enemic més proper o null.
     */
    public Objecte closestEnemic() {
        Objecte closestenemy;
        closestenemy = estat.enemic[0];

        for (int i = 0; i < estat.numEnemics; i++) {
            if (estat.enemic[i].agafaDistancia() < closestenemy.agafaDistancia()) {
                closestenemy = estat.enemic[i];
            }

        }

        return closestenemy;
    }

    /**
     * Cerca la mina més propera i retorna l'objecte.
     *
     * @return Objecte de la mina més propera o null.
     */
    public Objecte closestMina() {
        Objecte closestMina;
        closestMina = estat.mina[0];

        for (int i = 0; i < estat.numMines; i++) {
            if (estat.mina[i].agafaDistancia() < closestMina.agafaDistancia()) {
                closestMina = estat.mina[i];
            }

        }

        return closestMina;
    }

    /**
     * Comprova quina és la distància mínima dels tres visors.
     *
     * @return Distància mínima dels visors.
     */
    double minimaDistanciaVisors() {
        double minim;

        minim = Double.POSITIVE_INFINITY;
        if (estat.objecteVisor[ESQUERRA] == PARET) {
            minim = estat.distanciaVisors[ESQUERRA];
        }
        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < minim) {
            minim = estat.distanciaVisors[CENTRAL];
        }
        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < minim) {
            minim = estat.distanciaVisors[DRETA];
        }
        return minim;
    }

    /**
     * Comprova a quin costat està la paret més propera.
     *
     * @return ESQUERRA per defecte i DRETA si la paret més propera està a
     * aquest costat.
     */
    int paretMesPropera() {
        int mesPropera;
        double minim;

        minim = Double.POSITIVE_INFINITY;
        mesPropera = ESQUERRA;
        if (estat.objecteVisor[ESQUERRA] == PARET) {
            minim = estat.distanciaVisors[ESQUERRA];
        }
        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < minim) {
            minim = estat.distanciaVisors[DRETA];
            mesPropera = DRETA;
        }
        return mesPropera;
    }

    /**
     * Detecta la mina més propera i maniobra per tractar d'evitar-la.
     */
    void esquivarMina() {
        Objecte mina = closestMina();
        int minas = 0;
        if (mina != null) {
            if (mina.agafaSector() == 3 && mina.agafaDistancia() < 20) {
                minas += 1;
            }
            if (estat.objecteVisor[CENTRAL] == MINA && mina.agafaDistancia() < 30) {
                minas += 2;
            }
            if (mina.agafaSector() == 2 && mina.agafaDistancia() < 20) {
                minas += 4;
            }
            switch (minas) {
                case 0:
                    endavant();
                    break;
                case 1:
                case 3: //mina a l'esquerra
                    gira(-90);
                    break;
                case 4:
                case 6: //mina a la dreta
                    gira(90);
                    break;
                case 5:
                case 2:
                case 7: //mina davant
                    gira(90);
                    enrere();
                    espera = 2;
                    break;
            }
        }

    }

    /**
     * Comprova si l'estan disparant i si això es compleix, activa el mode
     * "evasió". El que fa és accelerar i realitzar una trajectòria en ziga
     * zaga.
     */
    void evasio() {
        if (estat.impactesRebuts > anteriorImpactes || enEvasio) {
            posaVelocitatLineal(VELOCITAT_LINEAL_COMBAT);
            enEvasio = true;
            tempsEvasio++;
            if (tempsEvasio > 75) {
                enEvasio = false;
                posaVelocitatLineal(VELOCITAT_LINEAL_NORMAL);
                tempsEvasio = 0;
            }
            int gir = tempsEvasio % 15;
            if (gir < 7) {
                esquerra();
            } else if (gir < 9) {
                endavant();
            } else {
                dreta();
            }
            if (estat.impactesRebuts == 4) {
                hyperespai(); //Quan està a un impacte de morir fa hyperespai
            }
        }
    }
}
