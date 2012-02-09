package org.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestCliente extends TFTP{

	public void main(String[] args) throws FileNotFoundException
	{
		int opcion=0;
		byte[] nombreArchivo;
		String hostServer="";	//<-------?
		
		String cadenaLeida;
        System.out.println("Ejecución de cliente:\n");
        System.out.println("Escriba \"salir\" para terminar la aplicacion.\n");
		System.out.print("tftp> ");
		Scanner sc = new Scanner(System.in);
		cadenaLeida = sc.nextLine();
		while(cadenaLeida.compareTo("Salir")!=0)
		{
			if(cadenaLeida.compareTo("get")==0)
				opcion = 1;
			if(cadenaLeida.compareTo("put")==0)
				opcion = 2;
		}
		if(opcion!=0)
		{
			System.out.println("Introduzca el nombre del archivo");
			cadenaLeida = sc.nextLine();
			nombreArchivo = cadenaLeida.getBytes();
			if(opcion==1)
				get(hostServer,nombreArchivo.toString());
			if(opcion==2)
			{
				File archivo = new File(nombreArchivo.toString());	//Crea uno nuevo sobreescribiendo el anterior?
				put(hostServer,archivo);
			}
		}
		else
		{
			System.out.println("Comando no válido. Escriba put para enviar un archivo o get para recibirlo\n");
		}
			
	}
	
	public void get(String hostServer, String nombreArchivo)
	{
		byte[] paquete = crearPaqueteRRQoWRQ(nombreArchivo, 1);
		if(enviarPaquete(hostServer, 69, paquete)==false)
			System.out.println("No se ha podido realizar la operacion");
		obtenerArchivo(null, null);
		///////////////////////////...
	}
	
	public void put(String hostServer, File archivo)
	{
		//Para un archivo de menos de 512B y sin perdida de paquetes
		
		
		byte[] paquete = crearPaqueteRRQoWRQ(archivo.getAbsolutePath(), 2);
		//Se envia la peticion WRQ
		if(enviarPaquete(hostServer, 69, paquete)==false)	
			System.out.println("No se ha podido realizar la operacion");	
		
		byte[] recibir=new byte[4];
		byte[] recibido;
		//Se recibe el paquete de respuesta
		recibido = recibirPaquete(recibir);
		if(catalogarPaquete(recibido)==4)	//Se recibide un ACK
		{
			if(desempaquetarACK(recibido)==0);	//Numero de bloque=0		
				moverArchivo(hostServer, 69, archivo);	//Envia el paquete de datos
		}
		if(catalogarPaquete(recibido)==5)
			System.out.println(desempaquetarError(recibido));
		
		//Se recibe un nuevo ACK
		recibido = recibirPaquete(recibir);
		if(catalogarPaquete(recibido)==4)	//Si recibide un ACK termina
			return;
		
		if(catalogarPaquete(recibido)==5)
			System.out.println(desempaquetarError(recibido));		
	}
}
