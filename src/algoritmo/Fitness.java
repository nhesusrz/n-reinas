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

import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

public class Fitness extends FitnessFunction {

    private final Genetico algoritmo;

    public Fitness(final Genetico algoritmo){
        this.algoritmo = algoritmo;
    }
    // Retorna el valor de la funcion de fitness. De acuerdo a lo planeado, retorna
    // la suma de conflictos entre las damas en sus respectivas diagonales. Cuanto menor
    // sea la cantidad de conflictos mejor sera.
    protected double evaluate(final IChromosome cromosoma){
        double s = 0;
        Gene[] genes = cromosoma.getGenes();
        int max = genes.length;
        for (int i = 0; i < max - 1; i++) {
            for (int j = i + 1; j < max; j++) {
                if((i!=j) && algoritmo.conflictosDiagonal(genes[i], genes[j], i, j))
                    s ++;
            }
        }
        return Math.max(1, algoritmo.maxCruzamiento() - s);
    }
}
