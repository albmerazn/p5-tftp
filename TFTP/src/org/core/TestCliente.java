package org.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestCliente
{
	TFTP tftp;
	
	private TestCliente()
	{
		tftp=new TFTP();//Utilizar el otro constructor
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		TestCliente cliente=new TestCliente();
		
		int opcion=0;
		byte[] nombreArchivo;
		String hostServer="";	//<-------?
		
		String cadenaLeida;
        System.out.println("Ejecución del cliente:\n");
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
		if(cadenaLeida.compareTo("Salir")==0)
			return;
		if(opcion!=0)
		{
			System.out.println("Introduzca el nombre del archivo");
			cadenaLeida = sc.nextLine();
			nombreArchivo = cadenaLeida.getBytes();
			if(opcion==1)
				cliente.get(hostServer,nombreArchivo.toString());
			if(opcion==2)
			{
				File archivo = new File(nombreArchivo.toString());	//Crea uno nuevo sobreescribiendo el anterior?
				cliente.put(hostServer,archivo);
			}
		}
		else
		{
			System.out.println("Comando no válido. Escriba put para enviar un archivo o get para recibirlo\n");
		}
			
	}

	private void get(String hostServer, String nombreArchivo)
	{
		byte[] paquete = tftp.crearPaqueteRRQoWRQ(nombreArchivo, 1);
		if(tftp.enviarPaquete(hostServer, 69, paquete)==false)
			System.out.println("No se ha podido realizar la operacion");
		tftp.obtenerArchivo(null, null);
		///////////////////////////...
	}
	
	public void put(String hostServer, File archivo)
	{
		//Para un archivo de menos de 512B y sin perdida de paquetes
		
		
		byte[] paquete = tftp.crearPaqueteRRQoWRQ(archivo.getAbsolutePath(), 2);
		//Se envia la peticion WRQ
		if(tftp.enviarPaquete(hostServer, 69, paquete)==false)	
			System.out.println("No se ha podido realizar la operacion");	
		
		byte[] recibir=new byte[4];
		byte[] recibido;
		//Se recibe el paquete de respuesta
		recibido = tftp.recibirPaquete(recibir);
		if(tftp.catalogarPaquete(recibido)==4)	//Se recibide un ACK
		{
			if(tftp.desempaquetarACK(recibido)==0);	//Numero de bloque=0		
			tftp.moverArchivo(hostServer, 69, archivo);	//Envia el paquete de datos
		}
		if(tftp.catalogarPaquete(recibido)==5)
			System.out.println(tftp.desempaquetarError(recibido));
		
		//Se recibe un nuevo ACK
		recibido = tftp.recibirPaquete(recibir);
		if(tftp.catalogarPaquete(recibido)==4)	//Si recibide un ACK termina
			return;
		
		if(tftp.catalogarPaquete(recibido)==5)
			System.out.println(tftp.desempaquetarError(recibido));		
	}
		
}
