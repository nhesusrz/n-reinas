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

package manejadorArchivo;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

public class ManejadorTexto{

    public void escribirObjeto(Vector<String> lineas, File archivo, String extension) {
        try {
            FileWriter fichero = new FileWriter(archivo + extension);
            PrintWriter pw = new PrintWriter(fichero);
            for(Iterator<String> it = lineas.iterator(); it.hasNext();){
                String linea = it.next();
                pw.println(linea);
            }
            fichero.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

