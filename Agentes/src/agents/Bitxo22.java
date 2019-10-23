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
    long tempsColisio = 0;  //Temps que du el bitxo en col·lisió

    static final int ANGLE_VISORS_NORMAL = 40;
    static final int ANGLE_VISORS_COLISIO = 0;
    static final int DISTANCIA_VISORS_NORMAL = 300;
    static final int VELOCITAT_LINEAL_NORMAL = 4;
    static final int VELOCITAT_LINEAL_COMBAT = 6;
    static final int VELOCITAT_ANGULAR_NORMAL = 9;

    public Bitxo22(Agents pare) {
        super(pare, "Chop", "imatges/chop.gif");
    }

    @Override
    public void inicia() {
        posaAngleVisors(ANGLE_VISORS_NORMAL);        // 0 - 70
        posaDistanciaVisors(DISTANCIA_VISORS_NORMAL);   // 0 - 400
        posaVelocitatLineal(VELOCITAT_LINEAL_NORMAL);     // 1 - 6
        posaVelocitatAngular(VELOCITAT_ANGULAR_NORMAL);    // 1 - 9
        espera = 0;
        temps = 0;
    }

    @Override
    public void avaluaComportament() {
        int dir;
        Objecte mina;
        temps++;

        estat = estatCombat();
        if (espera > 0) {
            espera--;
        } else {
            atura();

            if (estat.enCollisio) // situació de nau bloquejada
            {
                tempsColisio++;
                if (tempsColisio == 5) {
                    posaAngleVisors(ANGLE_VISORS_COLISIO);
                }

                hyperespaiColisio(tempsColisio);
                // si veu la nau, dispara
                if (estat.objecteVisor[CENTRAL] == NAU && estat.bales > 0) {
                    dispara();   //bloqueig per nau, no giris dispara
                    tempsColisio = 0;
                } else // hi ha un obstacle, gira i parteix
                {
                    if (tempsColisio < 10) {
                        gira(40); // 40 graus
                    } else {
                        gira(-40);
                    }
                    if (hiHaParedDavant(15)) {
                        enrere();
                        espera = 3;
                    } else {
                        endavant();
                    }
                }
            } else {
                posaAngleVisors(ANGLE_VISORS_NORMAL);
                tempsColisio = 0;
                endavant();
                ObjecteMesProper();
                mina = closestMina();
                if (estat.objecteVisor[CENTRAL] == NAU && !estat.disparant && estat.bales > 5) {
                    dispara();
                }
                //Dodgear minas
                int minas = 0;
                if (mina != null) {
                    if (mina.agafaSector() == 3 && mina.agafaDistancia() < 35) {
                        minas += 1;
                    }
                    if (estat.objecteVisor[CENTRAL] == MINA && mina.agafaDistancia() < 45) {
                        minas += 2;
                    }
                    if (mina.agafaSector() == 2 && mina.agafaDistancia() < 35) {
                        minas += 4;
                    }
                    switch (minas) {
                        case 0:
                            endavant();
                            break;
                        case 1:
                        case 3: //mina a la izquierda
                            gira(-90);
                            break;
                        case 4:
                        case 6: //mina a la derecha
                            gira(90);
                            break;
                        case 5:
                            endavant();
                            break;
                        case 2:
                        case 7:
                            gira(90);
                            enrere();
                            espera = 2;
                            break;
                    }
                }

                // Miram els visors per detectar els obstacles
                int sensor = 0;

                if ((minas == 1 || minas == 3) || (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < 45)) {
                    sensor += 1;
                }
                if ((minas == 4 || minas == 6) || (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < 45)) {
                    sensor += 2;
                }
                if ((minas == 2 || minas == 7) || (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < 45)) {
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
                    case 2: // paret devant
                    case 7: // si estic molt aprop, torna enrere
                        double distancia;
                        distancia = minimaDistanciaVisors();

                        if (distancia < 15) {
                            esquerra();
                            enrere();
                            espera = 2;
                        } else {
                            switch (paretMesCercana()) {
                                case ESQUERRA:
                                    dreta();
                                case DRETA:
                                    esquerra();
                            }
                        }
                        break;
                }

            }

        }
    }

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

    public void ObjecteMesProper() {
        if (closestRecurs() != null && closestEnemic() != null) {

            if (closestEnemic().agafaDistancia() <= closestRecurs().agafaDistancia()) {
                mira(closestEnemic());
                posaVelocitatLineal(VELOCITAT_LINEAL_COMBAT);

            } else {
                mira(closestRecurs());
            }
        } else if (closestRecurs() != null && closestEnemic() == null) {
            mira(closestRecurs());
            posaVelocitatLineal(VELOCITAT_LINEAL_COMBAT);
        } else if (closestRecurs() == null && closestEnemic() != null) {
            mira(closestEnemic());
            posaVelocitatLineal(VELOCITAT_LINEAL_COMBAT);
        } else {
            posaVelocitatLineal(VELOCITAT_LINEAL_NORMAL);

        }

    }

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

    int paretMesCercana() {
        int mesCercana;
        double minim;

        minim = Double.POSITIVE_INFINITY;
        mesCercana = ESQUERRA;
        if (estat.objecteVisor[ESQUERRA] == PARET) {
            minim = estat.distanciaVisors[ESQUERRA];
        }
        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < minim) {
            minim = estat.distanciaVisors[DRETA];
            mesCercana = DRETA;
        }
        return mesCercana;
    }

    void hyperespaiColisio(long tempsColisio) {
        if (tempsColisio > 30 && estat.hyperespaiDisponibles > 0) {
            hyperespai();
        }
    }

    void evasio() {

    }
}
