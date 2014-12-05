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

import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.IntegerGene;

public class CodificadorDecodificador {

    private static CodificadorDecodificador instancia;
    private Genetico algoritmo;
    private IChromosome mejor_solucion;
    private String solucion_string;

    private CodificadorDecodificador(){}

    // Metodos Get.

    public Genetico obtenerAlgoritmo(){
        return algoritmo;
    }
    // Retorna el cromosoma con la mejor solucion.
    public IChromosome obtenerMejorSolucion(){
        return mejor_solucion;
    }
    // Retorna una representacion del tablero para una fila.
    public String obternerFilaString(int numero_fila){
        if (mejor_solucion != null){
            String fila = "";
            Gene[] genes = mejor_solucion.getGenes();
            boolean fin = false;
            int columna;
            for (columna = 0; columna < genes.length && !fin; columna++){
                int genValue = ((IntegerGene)genes[columna]).intValue();
                if (genValue == numero_fila)
                    fin = true;
            }
            columna--;
            for (int i = 0; i < columna; i++){
                fila += "[ ] ";
            }
            fila += "[D] ";
            columna++;
            for (int i = columna; i < genes.length; i++){
                fila += "[ ] ";
            }
            return fila;
        }
        return "";
    }
    // Retorna la solución de la evolucion como string.
    public String obtenerMejorSolucionString(){
        solucion_string = "";
        Gene[] genes = mejor_solucion.getGenes();
        for (int i = 0; i < genes.length - 1; i++){
            int valor_gen = ((IntegerGene)genes[i]).intValue();
            solucion_string += valor_gen + ", ";
        }
        int valor_gen = ((IntegerGene)genes[genes.length - 1]).intValue();
        solucion_string += valor_gen;
        return solucion_string;
    }
        
    // Metodos Set.

    public void cambiarSolucion(IChromosome solution){
        mejor_solucion = solution;
    }

    // Metodos generales.

    // Retorno una unica instancia de esta clase.
    public static CodificadorDecodificador obtenerInstancia(){
        if(instancia == null)
            instancia = new CodificadorDecodificador();
        return instancia;
    }
    // Inicializa los parametros del algoritmo.
    public void definirParametros(int poblacion, int numero_damas, float prob_cruce, float prob_mutacion, boolean activar_iteraciones, int iteraciones){
        algoritmo = new Genetico(numero_damas);
        algoritmo.cambiarMaxCantPoblacion(poblacion);
        algoritmo.cambiarProbMutacion(prob_mutacion);
        algoritmo.cambiarProbCruce(prob_cruce);
        if (activar_iteraciones){
            algoritmo.cambiarEstadoEvolIterada(true);
            algoritmo.cambiarMaxCantGeneraciones(iteraciones);
        }        
    }
    // Permite parar la evolucion que esta corriendo actualmente.
    public void detenerEvolucion(){
        algoritmo.detenerEvolucion();
    }
}
