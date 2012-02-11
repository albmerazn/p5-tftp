package org.core;

import java.io.IOException;
import java.util.Scanner;

public class TestCliente
{
	private TFTP tftp;
	private static String hostServidor;

	/**
	 * Constructor por defecto.
	 */
	private TestCliente()
	{
		tftp=new TFTP();
	}
	
	public static void main(String[] args) throws IOException
	{
		TestCliente cliente=new TestCliente();

		int opcion=0;
		String nombreArchivo;

		String cadenaLeida;
		System.out.println("Ejecuci�n del cliente:\n");
		System.out.println("Escriba \"salir\" para terminar la aplicacion.\n");
		System.out.print("tftp> ");
		Scanner sc = new Scanner(System.in);
		cadenaLeida = sc.nextLine();
		if(cadenaLeida.compareTo("Salir")!=0||cadenaLeida.compareTo("salir")!=0)
		{
			TestServidor.acabado=true;
			return;
		}
		if(cadenaLeida.compareTo("get")==0)
			opcion = 1;
		if(cadenaLeida.compareTo("put")==0)
			opcion = 2;
		else
			System.out.println("Comando no v�lido. Escriba put para enviar un archivo o get para recibirlo\n");

		if(opcion!=0)
		{
			System.out.println("Introduzca el nombre del archivo");
			cadenaLeida = sc.nextLine();
			nombreArchivo = cadenaLeida;
			if(opcion==1)
				cliente.get(hostServidor,nombreArchivo.toString());
			if(opcion==2)
				cliente.put(hostServidor,nombreArchivo);
		}
	}

	/**
	 * M�todo que transmite un archivo al servidor
	 * @param hostServer Nombre del host del servidor
	 * @param nombreArchivo Nombre del archivo a transmitir
	 */
	private void get(String hostServer, String nombreArchivo)
	{
		boolean fin=false;
		int numBloq=0;

		byte[] paquete = tftp.crearPaqueteRRQoWRQ(nombreArchivo, 1);
		//Se envia la peticion RRQ
		if(tftp.enviarPaquete(hostServer, 69, paquete)==false)	
			System.out.println("No se ha podido realizar la operacion");			

		byte[] recibir=new byte[4];
		byte[] recibido;


		while(fin!=true)
		{
			paquete = tftp.recibirPaquete(recibir);//Se recibe el paquete de respuesta
			if(tftp.catalogarPaquete(paquete)==3)	//Si es de datos
			{
				tftp.crearArchivo(nombreArchivo);
				if(tftp.escribirBytes(paquete,numBloq)!=true)	
					fin=true;

				recibido=tftp.crearPaqueteACK(numBloq);
				numBloq++;
				tftp.enviarPaquete(hostServer, 69, recibido);
			}
			if(tftp.catalogarPaquete(recibir)==5)
				System.out.println(tftp.desempaquetarError(recibir));
		}	
	}

	/**
	 * M�todo que recibe un archivo del servidor
	 * @param hostServer Nombre del host del servidor
	 * @param archivo Nombre del arhivo que se solicita
	 */
	private void put(String hostServer, String archivo)
	{
		boolean fin=false;
		int numACK;		//Utilizar para controlar la perdida de paquetes
		int numBloq=0;
		byte[] aux;

		byte[] paquete = tftp.crearPaqueteRRQoWRQ(archivo, 2);
		//Se envia la peticion WRQ
		if(tftp.enviarPaquete(hostServer, 69, paquete)==false)	
			System.out.println("No se ha podido realizar la operacion");	


		byte[] recibir=new byte[4];
		byte[] recibido;

		while(fin!=true)
		{
			recibido = tftp.recibirPaquete(recibir);//Se recibe el paquete de respuesta
			if(tftp.catalogarPaquete(recibido)==4)	//Si es un ACK
			{
				numACK=tftp.desempaquetarACK(recibido); //Control de los ACKs recibidos
				if(numACK!=numBloq)
					fin=true;
				tftp.cargarArchivo(archivo);
				aux=tftp.leerBytes(numBloq);
				if(aux==null)
					fin=true;
				paquete=tftp.crearPaqueteData(numBloq, aux);
				numBloq++;
				tftp.enviarPaquete(hostServer, 69, paquete);
			}
			if(tftp.catalogarPaquete(recibido)==5)
				System.out.println(tftp.desempaquetarError(recibido));
		}	
	}
}