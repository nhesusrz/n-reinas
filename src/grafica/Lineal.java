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

package grafica;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Lineal extends Grafica{

    private XYSeriesCollection cjto_datos;
    private  XYSeries ultima_serie;
    private JFreeChart grafico;
    private ChartPanel panel_grafico;
    private Integer num_evoluciones;

    public Lineal(){
        num_evoluciones = 0;
        inicializar();
    }

    public void crearNuevaSerie(){
        ultima_serie = crear_nueva_serie();
        grafico.getPlot().zoom(0.0);
    }

    public void actualizarSerie(double x, double y){
        ultima_serie.add(x, y);
    }

    public ChartPanel getPanelGrafico() {
        return panel_grafico;
    }

    public void destroy() {
        num_evoluciones = 0;
        cjto_datos.removeAllSeries();
        ultima_serie = null;
    }

    // Métodos privados

    private void inicializar(){
        cjto_datos = new XYSeriesCollection();
        grafico = crearGrafico(cjto_datos);
        panel_grafico = new ChartPanel(grafico);
        panel_grafico.setFillZoomRectangle(true);
        panel_grafico.setMouseWheelEnabled(true);
    }

    private static JFreeChart crearGrafico(XYSeriesCollection cjto_datos){
        JFreeChart grafico = ChartFactory.createXYLineChart(
            "Fitness vs. Generación",   // Título
            "Generaciones",             // Título eje x
            "Fitness",                  // Título eje y
            cjto_datos,                 // Datos
            PlotOrientation.VERTICAL,   // Orientación
            true,                       // Incluir leyenda
            true,                       // Incluir tooltips
            false                       // Incluir URLs
        );
        return grafico;
    }

    private XYSeries crear_nueva_serie(){
        num_evoluciones++;
        XYSeries serie_nueva = new XYSeries("Evolución " + num_evoluciones);
        cjto_datos.addSeries(serie_nueva);
        return serie_nueva;
    }
}
