package agents;

import java.util.Iterator;

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
        posaDistanciaVisors(200);   // 0 - 400
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
                } 
                else // hi ha un obstacle, gira i parteix
                {
                    gira(20); // gira 20 graus
                    if (hiHaParedDavant(10)) {
                        enrere();
                    } else {
                        endavant();
                    }
                    espera = 3;
                }
            } else {
                endavant();

                if (estat.objecteVisor[CENTRAL] == NAU && !estat.disparant) {
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
                    case 3:  // esquerra bloquejada
                        dreta();
                        break;
                    case 4:
                    case 6:  // dreta bloquejada
                        esquerra();
                        break;
                    case 5:
                        endavant();
                        break;  // centre lliure
                    case 2:  // paret devant
                    case 7:  // si estic molt aprop, torna enrere
                        double distancia;
                        distancia = minimaDistanciaVisors();

                        if (distancia < 10) {
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
    boolean cercarRecursos(int dist){
        boolean trobat=false;
        
        for (int i=0; i<estat.numRecursos;i++) {
            
        }
        return trobat;
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
    
    public Objecte ObjecteMesProper(Objecte r1,Objecte r2){
        if(r1.agafaDistancia()<=r2.agafaDistancia()){
            return r1;
        }else{
            return r2;
        }
        
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
