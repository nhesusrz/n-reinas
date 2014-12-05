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

package nreinas;

import algoritmo.Genetico;
import algoritmo.CodificadorDecodificador;
import grafica.Lineal;
import java.util.Observable;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Observer;
import java.util.Vector;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import manejadorArchivo.ManejadorTexto;
import org.jfree.ui.ExtensionFileFilter;

public class NReinasView extends FrameView implements Observer{

    public NReinasView(SingleFrameApplication app) {
        super(app);

        initComponents();

        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");

        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        iniciar_elementos();
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = NReinasApp.getApplication().getMainFrame();
            aboutBox = new NReinasAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        NReinasApp.getApplication().show(aboutBox);
    }

    @Action
    public void limpiarGrafica(){
        grafico_lineal.destroy();
        borrarInformacion();
        cantidad_evoluciones = 0;
        jButton1.setEnabled(false);
    }

    @Action
    public void grabarArchivo(){
        jFileChooser1.setCurrentDirectory(new File(".//EditorDeProyectos//proyectos"));
        int returnVal = jFileChooser1.showSaveDialog(jButton1);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File archivo = jFileChooser1.getSelectedFile();
            mtexto.escribirObjeto(vector_salida, archivo, ".txt");
        }        
    }

    @Action
    public void correrAlgoritmo(){
        cantidad_evoluciones++;
        if(!corriendo_algoritmo){
             if(sonParametrosValidos()){
                corriendo_algoritmo = true;
                mostrarInformacion(" ");
                mostrarInformacion("**** Evolución  " + cantidad_evoluciones + " ****");
                desabilitarBotones();
                grafico_lineal.crearNuevaSerie();
                // Seteo los parametros obtenidos desde la interfaz.
                CodificadorDecodificador.obtenerInstancia().definirParametros(((Integer)jSpinner5.getValue()).intValue(),
                                                                              ((Integer)jSpinner1.getValue()).intValue(),
                                                                              ((Double)jSpinner3.getValue()).floatValue()/100,
                                                                              ((Double)jSpinner4.getValue()).floatValue()/100,
                                                                              jCheckBox1.isSelected(),
                                                                              ((Integer)jSpinner2.getValue()).intValue());
                // Agrego a la interfaz como observador del thread del algoritmo.
                CodificadorDecodificador.obtenerInstancia().obtenerAlgoritmo().addObserver(this);
                // Creo el thread.
                Thread t = new Thread(CodificadorDecodificador.obtenerInstancia().obtenerAlgoritmo());
                // Seteo el tiempo justo antes de empezar a correr el algoritmo.
                tiempo_inicial = System.currentTimeMillis();
                // Corro el Thread y este invoca el metodo run de Genetico.
                t.start();
             }
        }
        else{
            // Finaliza el algoritmo cuando se presiona Stop.
            finalizarAlgoritmo();
        }
    }
    // El thread va actualizando los datos. Cuando se llega a un valor de fitness
    // potencialmente bueno o se llega a un numero de iteraciones definido se finaliza el algoritmo.
    public void update(Observable o, Object arg) {
        Genetico ag = (Genetico) o;
        double valor_fitness = ((Double)arg).doubleValue();        
        if(ag.activadaEvolucion()){
            // Actualizo el tiempo en que termina la n-esima generacion.
            tiempo_final = System.currentTimeMillis();
            // Actializo la grafica.
            grafico_lineal.actualizarSerie(ag.obtenerNumEvoluciones(),valor_fitness);
            // Muestro la información de la solucion.
            informacion = "\n" + " Mejor solución: " + CodificadorDecodificador.obtenerInstancia().obtenerMejorSolucionString() + "\n" + " Número de generaciones: " + ag.obtenerNumEvoluciones() + "\n" + " Tiempo: " + ((tiempo_final - tiempo_inicial) / 1000) + " segundos";
            mostrarInformacion(informacion);
            // Actualizo la barra de progreso.
            actualizarBarraProgreso();
            if(ag.evolucionLimitada() && (ag.obtenerNumEvoluciones() + 1 == ag.obtenerMaxCantEvolucion())){
                colocarBufferArchivo(ag);
                finalizarAlgoritmo();
            }
        }
        else{
            colocarBufferArchivo(ag);
            finalizarAlgoritmo();
        }
    }

    // Métodos privados

    // Va colocando en el buffer temporal para la posterior persistencia en archivo de las evoluciones.
    private void colocarBufferArchivo(Genetico ag){
        vector_salida.add(" ");
        vector_salida.add("Evolución  " + cantidad_evoluciones + ": ");
        vector_salida.add(" ");
        vector_salida.add(" Número de Damas: " + ag.obtenerNumeroDamas() + informacion);
        vector_salida.add(" ");
        vector_salida.add(" Decodificación: ");
        vector_salida.add(" ");
        for(int i = 0; i < ag.obtenerNumeroDamas(); i++){
            vector_salida.add("   " + CodificadorDecodificador.obtenerInstancia().obternerFilaString(i));
        }
    }
    // Chequea que los parametros ingresados sean validos.
    private boolean sonParametrosValidos(){
        if((((Double)jSpinner4.getValue()).floatValue() != 0.00) &&
          (((Integer)jSpinner1.getValue()).intValue() != 0) &&
          (((Double)jSpinner3.getValue()).floatValue())!= 0.00)
          return true;
        return false;
    }
    // Barra de Progreso.
    private void inicializarBarraProgreso(){
        cont_bar_progreso = 0;
        progressBar.setValue(cont_bar_progreso);
        progressBar.setMaximum(MAX_GEN);
        progressBar.repaint();
    }

    private void actualizarBarraProgreso(){
        cont_bar_progreso++;
        if(progressBar.getPercentComplete() == 1.0)
            progressBar.setMaximum(CodificadorDecodificador.obtenerInstancia().obtenerAlgoritmo().obtenerNumEvoluciones() * 2);
        progressBar.setValue(cont_bar_progreso);
        progressBar.repaint();
    }

    // Botones.

    // Metodo cambia el cubtitulo y el icono del boton 2. (Play y Stop).
    private void actualizarPropiedadesBoton2(String dir_icono, String toolTip){
        URL direccion;
        direccion = getClass().getClassLoader().getResource(dir_icono);
        ImageIcon icono = new ImageIcon(direccion);
        jButton2.setIcon(icono);
        jButton2.setToolTipText(toolTip);
    }

    // Habilitan o desabilitan botones cuando comienza a correr el algoritmo o es detenido.

    private void habilitarBotones(){
        actualizarPropiedadesBoton2("nreinas/resources/iconos/Correr.png", "Iniciar Evolución");
        if(!jButton1.isEnabled())
            jButton1.setEnabled(true);
        jButton4.setEnabled(true);
    }

    private void desabilitarBotones(){
        actualizarPropiedadesBoton2("nreinas/resources/iconos/Parar.png", "Detener Evolución");
        if(jButton1.isEnabled())
            jButton1.setEnabled(false);
        jButton4.setEnabled(false);
    }
    // Finaliza el algoritmo que se esta corriendo.
    private void finalizarAlgoritmo(){
        inicializarBarraProgreso();
        corriendo_algoritmo = false;        
        CodificadorDecodificador.obtenerInstancia().detenerEvolucion();
        habilitarBotones();
    }
    // Limpia la información temporal de las evoluciones.
    private void borrarInformacion(){
        jTextArea1.setText("");
        limpiarVectorTemporal();
    }
    // Limpia el vector que contiene las salidas temporalmente.
    private void limpiarVectorTemporal(){
        vector_salida = new Vector<String>();
        colocarEncabezadoArchivo();
    }
    // Muestra la información en el area de texto.
    private void mostrarInformacion(String texto){
        jTextArea1.append(texto + "\n");        
    }
    // Incializa los elementos cuando arranca la aplicación.
    private void iniciar_elementos(){
        // Barra de progreso
        progressBar.setVisible(true);
        progressBar.setMinimum(1);
        progressBar.setMaximum(MAX_GEN);
        cont_bar_progreso = 0;
        // Activacion de los botones.
        jButton1.setEnabled(false);        
        jSpinner2.setEnabled(false);        
        // Sincronización de las evoluciones.
        corriendo_algoritmo = false;
        cantidad_evoluciones = 0;
        // Elementos para grabar en el archivo.
        mtexto = new ManejadorTexto();
        vector_salida = new Vector<String>();
        colocarEncabezadoArchivo();
        // Le asigno un filtro al File Chooser.
        ExtensionFileFilter filtro_txt = new ExtensionFileFilter("(Formato texto) (*.txt)", ".txt");
        jFileChooser1.setFileFilter(filtro_txt);
    }
    // Crea el encabezado para el archivo de texto.
    private void colocarEncabezadoArchivo(){
        vector_salida.add("============================================");
        vector_salida.add("                  N-Reinas");
        vector_salida.add("      Facultad de Cs. Exactas - UNICEN");
        vector_salida.add("  Introducción a la Computación Evolutiva");
        vector_salida.add("     Copyright © 2010 Martín I. Pacheco");
        vector_salida.add(" E-Mail: mpacheco@alumnos.exa.unicen.edu.ar");
        vector_salida.add("============================================");
        vector_salida.add(" ");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        grafico_lineal = new grafica.Lineal();
        jScrollPane2 = new javax.swing.JScrollPane(grafico_lineal.getPanelGrafico());
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jCheckBox1 = new javax.swing.JCheckBox();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jSpinner3 = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jSpinner4 = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jSpinner5 = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jFileChooser1 = new javax.swing.JFileChooser();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(nreinas.NReinasApp.class).getContext().getResourceMap(NReinasView.class);
        mainPanel.setFont(resourceMap.getFont("mainPanel.font")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N

        jToolBar1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(nreinas.NReinasApp.class).getContext().getActionMap(NReinasView.class, this);
        jButton1.setAction(actionMap.get("grabarArchivo")); // NOI18N
        jButton1.setFont(resourceMap.getFont("jButton1.font")); // NOI18N
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);

        jButton2.setAction(actionMap.get("correrAlgoritmo")); // NOI18N
        jButton2.setFont(resourceMap.getFont("jButton2.font")); // NOI18N
        jButton2.setIcon(resourceMap.getIcon("jButton2.icon")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setToolTipText(resourceMap.getString("jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        jButton4.setAction(actionMap.get("limpiarGrafica")); // NOI18N
        jButton4.setFont(resourceMap.getFont("jButton4.font")); // NOI18N
        jButton4.setIcon(resourceMap.getIcon("jButton4.icon")); // NOI18N
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setToolTipText(resourceMap.getString("jButton4.toolTipText")); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton4);

        jButton3.setAction(actionMap.get("showAboutBox")); // NOI18N
        jButton3.setFont(resourceMap.getFont("jButton3.font")); // NOI18N
        jButton3.setIcon(resourceMap.getIcon("jButton3.icon")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setToolTipText(resourceMap.getString("jButton3.toolTipText")); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton3);

        jButton5.setAction(actionMap.get("quit")); // NOI18N
        jButton5.setFont(resourceMap.getFont("jButton5.font")); // NOI18N
        jButton5.setIcon(resourceMap.getIcon("jButton5.icon")); // NOI18N
        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setToolTipText(resourceMap.getString("jButton5.toolTipText")); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton5);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanel1.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel1.border.titleFont"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanel2.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel2.border.titleFont"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jSpinner1.setFont(resourceMap.getFont("jSpinner4.font")); // NOI18N
        jSpinner1.setName("jSpinner1"); // NOI18N

        jCheckBox1.setFont(resourceMap.getFont("jCheckBox1.font")); // NOI18N
        jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N
        jCheckBox1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox1StateChanged(evt);
            }
        });

        jSpinner2.setFont(resourceMap.getFont("jSpinner4.font")); // NOI18N
        jSpinner2.setName("jSpinner2"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jSpinner3.setFont(resourceMap.getFont("jSpinner4.font")); // NOI18N
        jSpinner3.setName("jSpinner3"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jSpinner4.setFont(resourceMap.getFont("jSpinner4.font")); // NOI18N
        jSpinner4.setName("jSpinner4"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jSpinner5.setFont(resourceMap.getFont("jSpinner4.font")); // NOI18N
        jSpinner5.setName("jSpinner5"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setBackground(resourceMap.getColor("jTextArea1.background")); // NOI18N
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(resourceMap.getFont("jTextArea1.font")); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setAutoscrolls(false);
        jTextArea1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTextArea1.setDisabledTextColor(resourceMap.getColor("jTextArea1.disabledTextColor")); // NOI18N
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        jLabel6.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jCheckBox1)
                            .addComponent(jLabel1))
                        .addGap(56, 56, 56)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSpinner2, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                            .addComponent(jSpinner4, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                            .addComponent(jSpinner3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                            .addComponent(jSpinner5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)))
                    .addComponent(jLabel6))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jSpinner5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jSpinner4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(2, 2, 20000, 1));
        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(1, 1, 20000, 1));
        jSpinner3.setModel(new javax.swing.SpinnerNumberModel(0.00, 0.00, 1.00, 0.01));
        jSpinner4.setModel(new javax.swing.SpinnerNumberModel(0.00, 0.00, 1.00, 0.01));
        jSpinner5.setModel(new javax.swing.SpinnerNumberModel(0, 0, 20000, 1));

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addContainerGap(642, Short.MAX_VALUE))
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        jFileChooser1.setName("jFileChooser1"); // NOI18N

        setComponent(mainPanel);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox1StateChanged
        if(jCheckBox1.isSelected())
            jSpinner2.setEnabled(true);
        else
            jSpinner2.setEnabled(false);
    }//GEN-LAST:event_jCheckBox1StateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JSpinner jSpinner5;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    private Lineal grafico_lineal;

    private boolean corriendo_algoritmo;
    private long tiempo_inicial, tiempo_final;
    private int cantidad_evoluciones;
    private Vector<String> vector_salida;
    private ManejadorTexto mtexto;
    private String informacion;
    private int cont_bar_progreso;

    private final int MAX_GEN = 30;
}
