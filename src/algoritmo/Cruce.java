/*
 *  N-Reinas. Trabajo para final de la materia Introducción a la Computación
 *  Evolutiva. Permite correr un algoritmo genético para resolver el problema
 *  la conocido con el nombre de N-Reinas.
 *  Copyright (C) 2010 Martín I. Pacheco
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Correo electrónico: mpacheco@alumnos.exa.unicen.edu.ar
 */

package algoritmo;

import java.util.List;

import org.jgap.BaseGeneticOperator;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.RandomGenerator;

public class Cruce extends BaseGeneticOperator{

    public final int OFFSET = 1;
    public final double POR_CRUCE = 0.8;

    private int offset_comienzo;
    private double porcentaje_cruce;

    public Cruce() throws InvalidConfigurationException{
        super(Genotype.getStaticConfiguration());
        offset_comienzo = 1;
        porcentaje_cruce = 0.8;
    }

    public Cruce(Configuration configuracion) throws InvalidConfigurationException{
        super(configuracion);
        offset_comienzo = 1;
        porcentaje_cruce = 0.8;
    }

    public Cruce(final Configuration configuracion, final double porcentaje_cruce) throws InvalidConfigurationException{
        super(configuracion);
        this.porcentaje_cruce = porcentaje_cruce;
    }

    // Metodo Get.

    public int obtenerOffsetComienzo(){
        return offset_comienzo;
    }

    // Metodos Set.

    public void cambiarRateCruce(double numero){
        porcentaje_cruce = numero;
    }

    public void cambiarComienzoOffset(int offset) {
        offset_comienzo = offset;
    }

    // Metodos generales.

    public void operate(final Population poblacion, final List cromosomas_candidatos){
            int tamaño = Math.min(getConfiguration().getPopulationSize(), poblacion.size());
            int num_cromosomas = (int) (tamaño * porcentaje_cruce) / 2;
            RandomGenerator generador = getConfiguration().getRandomGenerator();
            for (int i = 0; i < num_cromosomas; i++) {
                IChromosome primer_conflicto = (IChromosome) poblacion.getChromosome(generador.nextInt(tamaño)).clone();
                IChromosome segundo_conflicto = (IChromosome) poblacion.getChromosome(generador.nextInt(tamaño)).clone();
                operate(primer_conflicto, segundo_conflicto);
                cromosomas_candidatos.add(primer_conflicto);
                cromosomas_candidatos.add(segundo_conflicto);
            }
    }

    public void operate(final IChromosome primer_conflicto,final IChromosome segundo_conflicto){
        Gene[] g1 = primer_conflicto.getGenes();
        Gene[] g2 = segundo_conflicto.getGenes();
        Gene[] c1, c2;
        try {
            c1 = operate(g1, g2);
            c2 = operate(g2, g1);
            primer_conflicto.setGenes(c1);
            segundo_conflicto.setGenes(c2);
        } catch (InvalidConfigurationException cex) {
                throw new Error("Error ocurrido en el operador sobre los genes:" + primer_conflicto
                                + " y " + segundo_conflicto + " con punto de cruce: " + offset_comienzo
                                + " geens fueron exclu�dos "
                                + " por operador de cruce. Mensaje de error: " + cex.getMessage());
        }
   }

   protected Gene[] operate(final Gene[] g1, final Gene[] g2){
        int tamaño = g1.length;
        Gene[] g1_primero = new Gene[offset_comienzo];
        Gene[] g2_segundo = new Gene[offset_comienzo];
        Gene[] g2_ultimo = new Gene[tamaño - offset_comienzo];
        Gene[] gen_hijo = new Gene[tamaño];
        g1_primero = copiarPrimeraParteGen(g1);
        g2_segundo = copiarPrimeraParteGen(g2);
        g2_ultimo = copiarUltimaParteGen(g2);
        for(int i = 0; i < offset_comienzo; i++)
            gen_hijo[i] = g1_primero[i];
        int i = 0;
        int j = offset_comienzo;
        while (i < g2_ultimo.length && j < tamaño){
            if (!contieneGen(gen_hijo,cantElementos(gen_hijo), g2_ultimo[i])) {
                gen_hijo[j] = g2_ultimo[i];
                j++;
            }
            i++;
        }
        if (cantElementos(gen_hijo) < gen_hijo.length){
            int k = 0;
            int l = cantElementos(gen_hijo);
            while (k < g1_primero.length && l < tamaño){
                if (!contieneGen(gen_hijo, cantElementos(gen_hijo),g2_segundo[k])) {
                    gen_hijo[l] = g2_segundo[k];
                    l++;
                }
                k++;
            }
        }
        return gen_hijo;
    }

    protected Gene[] copiarPrimeraParteGen(final Gene[] gen){
        Gene[] g_primero = new Gene[offset_comienzo];
        for (int i = 0; i < offset_comienzo; i++)
            g_primero[i] = gen[i];
        return g_primero;
    }

    protected Gene[] copiarUltimaParteGen(final Gene[] gen){
        Gene[] g_ultimo = new Gene[gen.length - offset_comienzo];
        int i = 0;
        int j = offset_comienzo;
        while (i < gen.length - offset_comienzo && j < gen.length) {
            g_ultimo[i] = gen[j];
            i++;
            j++;
        }
        return g_ultimo;
    }

    protected boolean contieneGen(final Gene[] gen,final int tamaño_gen,final  Gene gen_x) {
        for (int i = 0; i < tamaño_gen; i++) {
             if (gen[i].equals(gen_x))
                    return true;
        }
        return false;
    }

    protected int cantElementos(final Gene[] gen){
        int cant = 0;
        boolean fin = true;
        for (int i = 0; i <gen.length && fin; i++) {
            if(gen[i]!=null)
                cant++;
        }
        return cant;
    }

    public int compareTo(final Object objeto) {
            if (objeto == null)
                return 1;
            Cruce op = (Cruce) objeto;
            if(obtenerOffsetComienzo() < op.obtenerOffsetComienzo())
                return 1;
            else
                if(obtenerOffsetComienzo() > op.obtenerOffsetComienzo())
                    return -1;
                else
                    return 0;
    }
}

