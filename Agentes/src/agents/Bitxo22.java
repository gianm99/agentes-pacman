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

    public Bitxo22(Agents pare) {
        super(pare, "Chop", "imatges/chop.gif");
    }

    @Override
    public void inicia() {
        posaAngleVisors(40);        // 0 - 70
        posaDistanciaVisors(300);   // 0 - 400
        posaVelocitatLineal(5);     // 1 - 6
        posaVelocitatAngular(4);    // 1 - 9
        espera = 0;
        temps = 0;
    }

    @Override
    public void avaluaComportament() {
        int dir;

        temps++;
        estat = estatCombat();
        if (espera > 0) {
            espera--;
        } else {
            atura();

            if (estat.enCollisio) // situació de nau bloquejada
            {
                // si veu la nau, dispara

                if (estat.objecteVisor[CENTRAL] == NAU) {
                    dispara();   //bloqueig per nau, no giris dispara
                } else // hi ha un obstacle, gira i parteix
                {
                    gira(20); // 20 graus
                    if (hiHaParedDavant(10)) {
                        enrere();
                    } else {
                        endavant();
                    }
                    espera = 3;
                }
            } else {

                endavant();
                ObjecteMesProper();

                if (estat.objecteVisor[CENTRAL] == NAU && !estat.disparant && estat.bales>7) {
                    dispara();
                }
                // Miram els visors per detectar els obstacles
                int sensor = 0;

                if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < 45) {
                    sensor += 1;
                }
                if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < 45) {
                    sensor += 2;
                }
                if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < 45) {
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
                            espera = 8;
                            enrere();
                        } else {
                            esquerra();
                        }
                        break;
                }

            }

        }
    }

    boolean cercarRecursos(int dist) {
        boolean trobat = false;

        for (int i = 0; i < estat.numRecursos; i++) {

        }
        return trobat;
    }

    boolean hiHaParedDavant(int dist) {

        if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] <= dist) {
            return true;
        }

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

            } else {
                mira(closestRecurs());
            }
        } else if (closestRecurs() != null && closestEnemic() == null) {
            mira(closestRecurs());
        } else if (closestRecurs()==null && closestEnemic()!=null) {
            mira(closestEnemic());
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
}
