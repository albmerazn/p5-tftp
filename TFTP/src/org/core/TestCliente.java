package org.core;

import java.io.File;

public class TestCliente extends TFTP{

	public static void main(String[] args)
	{
		
	}
	
	private void get(String hostServer, String nombreArchivo)
	{
		byte[] paquete = crearPaqueteRRQoWRQ(nombreArchivo, 1);
		if(enviarPaquete(hostServer, 69, paquete)==false)
			System.out.println("No se ha podido realizar la operacion");
		obtenerArchivo(null, null);
		///////////////////////////...
	}
	
	private void put(String hostServer, File archivo)
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
