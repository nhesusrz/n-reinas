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

import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.DefaultFitnessEvaluator;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.RandomGenerator;
import org.jgap.event.EventManager;
import org.jgap.impl.BestChromosomesSelector;
import org.jgap.impl.ChromosomePool;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.StockRandomGenerator;
import org.jgap.impl.SwappingMutationOperator;
import org.jgap.impl.TournamentSelector;

public class Genetico extends Observable implements Runnable{

    private final int MAX_GEN = 10000;
    private final int MAX_POB = 100;

    private Configuration configuracion;
    private int max_cant_generaciones, max_cant_poblacion;
    private int num_damas;
    private boolean evolucion_iterada;
    private int num_evoluciones;
    private float prob_mutacion, prob_cruzamiento;
    private int max_cruzamiento;
    private boolean evolucionActivada;

  /*  public Genetico(){
        inicializarMaxCruzamientos();
        max_cruzamiento = 0;
        max_cant_generaciones = MAX_GEN;
        max_cant_poblacion = MAX_POB;
    }*/

    public Genetico(int numero_damas){
        num_damas = numero_damas;
        evolucionActivada = true;
        num_evoluciones = 0;
        max_cant_generaciones = MAX_GEN;
        max_cant_poblacion = MAX_POB;
        inicializarMaxCruzamientos();
    }

    // Metodos Get.

    // Retorna la cantidad maxima de cruces.
    public int maxCruzamiento(){
        return max_cruzamiento;
    }
    // Retorna l aconfiguracion.
    public Configuration obtenerConfiguracion(){
        return configuracion;
    }
    // Retorna el maximo de la evolucion.
    public int obtenerMaxCantEvolucion(){
        return max_cant_generaciones;
    }
    // Retorna el maximo del tamaño de la poblacion.
    public int obtenerMaxCantPoblacion(){
        return max_cant_poblacion;
    }
    // Retorna el numero de reinas en la solucion.
    public int obtenerNumeroDamas(){
        return this.num_damas;
    }
    // Retorna la probabilidad de mutacion.
    public float obtenerProbMutacion(){
        return prob_mutacion;
    }
    // Retorna el numero de evoluvoluciones.
    public int obtenerNumEvoluciones(){
        return num_evoluciones;
    }
    // Retorna la probabilidad de cruce.
    public float obtenerProbCruce(){
        return prob_cruzamiento;
    }

    // Metodos Set.

    // Permite cambiar la configuracion.
    public void cambiarConfiguracion(Configuration configuracion){
        this.configuracion = configuracion;
    }
    // Permite cambiar el parametro de maxima evoluvion.
    public void cambiarMaxCantGeneraciones(int num_generaciones){
        max_cant_generaciones = num_generaciones;
    }
    // Permite cambiar el tamaño de la poblacion.
    public void cambiarMaxCantPoblacion(int cant_poblacion){
        max_cant_poblacion = cant_poblacion;
    }
    // Permite cambiar el estado del limite de la evolucion.
    public void cambiarEstadoEvolIterada(boolean estado){
        evolucion_iterada = estado;
    }
    // Permite cambiar el factor de mutacion.
    public void cambiarProbMutacion(float probabilidad){
        prob_mutacion = probabilidad;
    }
    // Permite cambiar el numero de evoluviones.
    public void cambiarNumEvoluciones(int numero){
        num_evoluciones = numero;
    }
    // Permite cambiar el estado de activacion de la evolucion.
    public void cambiarEstadoEvoluciones(boolean estado){
        evolucionActivada = estado;
    }
    // Permite cambiar la probabilidad de cruce.
    public void cambiarProbCruce(float probabilidad){
        prob_cruzamiento = probabilidad;
    }

    // Metodos generales.

    // Crea un cromosoma que representa la lista de reinas.
    public IChromosome crearCromosomaMuestra(){
        try {
            Gene[] genes = new Gene[this.obtenerNumeroDamas()];
            for (int i = 0; i < genes.length; i++) {
                genes[i] = new IntegerGene(this.obtenerConfiguracion(), 0, this.obtenerNumeroDamas() - 1);
                genes[i].setAllele(new Integer(i));
            }
            IChromosome sample = new Chromosome(this.obtenerConfiguracion(), genes);
            return sample;
        } catch (InvalidConfigurationException iex){
            throw new IllegalStateException(iex.getMessage());
        }
    }
    // Crea la funcion de fitness a usar.
    public FitnessFunction crearFuncionFitness(){
        return new Fitness(this);
    }
    // Crea la configuracion.
    public Configuration crearConfiguracion() throws InvalidConfigurationException{
        Configuration.reset();
        Configuration configuracion = new Configuration();

        BestChromosomesSelector bestChromsSelector = new BestChromosomesSelector(configuracion, 1.0d);
        bestChromsSelector.setDoubletteChromosomesAllowed(true);

        TournamentSelector tournamentSelector = new TournamentSelector(configuracion, 2, 0.8);
        tournamentSelector.setDoubletteChromosomesAllowed(false);

        configuracion.addNaturalSelector(tournamentSelector, true);
        configuracion.addNaturalSelector(bestChromsSelector, false);

        configuracion.setRandomGenerator(new StockRandomGenerator());
        configuracion.setMinimumPopSizePercent(100);

        configuracion.setEventManager(new EventManager());

        configuracion.setFitnessEvaluator(new DefaultFitnessEvaluator());

        configuracion.setChromosomePool(new ChromosomePool());
        // Operadores geneticos.
        Cruce operador_cruce = new Cruce(configuracion, prob_cruzamiento);
        RandomGenerator generador = configuracion.getRandomGenerator();
        // Genera un punto aleatorio para realizar cruce.
        int punto = generador.nextInt(num_damas - 1);
        operador_cruce.cambiarComienzoOffset(punto);
        configuracion.addGeneticOperator(operador_cruce);

        SwappingMutationOperator operador_mutacion_intercambio = new SwappingMutationOperator(configuracion, (int)(1 / prob_mutacion));
        operador_mutacion_intercambio.setStartOffset(0);
        configuracion.addGeneticOperator(operador_mutacion_intercambio);

        return configuracion;
    }
    // Ejecuta el algoritmo genetico para encontrar la solucion.
    public IChromosome buscarSolucion() throws Exception {
        // Creo la configuración de los componentes del algoritmo.
        configuracion = crearConfiguracion();
        FitnessFunction funcion_fitnesss = crearFuncionFitness();
        configuracion.setFitnessFunction(funcion_fitnesss);
        // Indico al Configurador como seran configurados los cromosomas.
        // Creo un cromosoma de muestra que servira como guia para la creación de los demas.
        IChromosome cromosoma_muestra = crearCromosomaMuestra();
        configuracion.setSampleChromosome(cromosoma_muestra);
        // Indico al configurador con cuantos cromosomas inicializo la poblacion.
        configuracion.setPopulationSize(obtenerMaxCantPoblacion());
        // Creo los cromosomas randomicamente.
        IChromosome[] cromosomas = new IChromosome[configuracion.getPopulationSize()];
        Gene[] muestra_genes = cromosoma_muestra.getGenes();
        for (int i = 0; i < cromosomas.length; i++) {
          Gene[] genes = new Gene[muestra_genes.length];
          for (int k = 0; k < genes.length; k++) {
            genes[k] = muestra_genes[k].newGene();
            genes[k].setAllele(muestra_genes[k].getAllele());
          }
          cromosomas[i] = new Chromosome(configuracion, genes);
        }
        // Creo el genotipo.
        Genotype poblacion = new Genotype(configuracion, new Population(configuracion, cromosomas));
        IChromosome mejor_cromosoma = null;
        // Itero hasta alcanzar un fitness optimo o hasta una cierta cantidad de iteraciones si se indico.
        for(num_evoluciones = 0; (num_evoluciones < max_cant_generaciones || !evolucion_iterada) && evolucionActivada; num_evoluciones++){
            poblacion.evolve();
            mejor_cromosoma = poblacion.getFittestChromosome();
            CodificadorDecodificador.obtenerInstancia().cambiarSolucion(mejor_cromosoma);
            // Notifico a los observadores, en este caso a la interfaz principal.
            setChanged();
            notifyObservers(new Double(mejor_cromosoma.getFitnessValue()));
            if(mejor_cromosoma.getFitnessValue() == maxCruzamiento()){
                evolucionActivada = false;
                // Notifico a los observadores, en este caso a la interfaz principal.
                setChanged();
                notifyObservers(new Double(mejor_cromosoma.getFitnessValue()));
            }
        }
        num_evoluciones --;
        // Retorno la mejor solución encontrada.
        return mejor_cromosoma;
    }
    // Retorna si dos reinas se cruzan en forma diagonal.
    public boolean conflictosDiagonal(Gene dama1, Gene dama2,int columna_dama1,int columna_dama2) {
        IntegerGene gen_1 = (IntegerGene) dama1;
        IntegerGene gen_2 = (IntegerGene) dama2;
        int fila_dama1 = gen_1.intValue();
        int fila_dama2 = gen_2.intValue();
        return(Math.abs(fila_dama1 - fila_dama2) == Math.abs(columna_dama1 - columna_dama2));
    }
    // Dice si la evoluvion esta limitada.
    public boolean evolucionLimitada(){
        return evolucion_iterada;
    }
    // Permite detener la evolucion.
    public void detenerEvolucion() {
        evolucionActivada = false;
    }
    // Metodo de ejecucion del thread.
    public void run() {
        try {
            buscarSolucion();
        } catch (Exception ex) {
            Logger.getLogger(Genetico.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // Dice si la evolucion esta activada.
    public boolean activadaEvolucion(){
        return evolucionActivada;
    }

    // Metodos privados

    // Inicializa la cantidad maxima de cruces.
    private void inicializarMaxCruzamientos(){
        for(int i = num_damas - 1; i > 0; i--)
            max_cruzamiento += i;
    }

}
