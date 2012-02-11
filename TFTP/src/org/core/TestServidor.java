package org.core;

public class TestServidor {

	private String ruta;	//Directorio del servidor
	private String hostCliente;
	private int puertoCliente;
	private String archivo;	//Nombre del archivo a transmitir/recibir
	private TFTP tftp;
	public static boolean acabado;

	private TestServidor()
	{
		tftp = new TFTP(69,ruta);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestServidor servidor = new TestServidor();
		byte[] recibir=new byte[4];
		byte[] recibido;
		acabado=false;

		System.out.println("Ejecución del servidor\n");
		
		while(acabado!=true)
		{
			recibido = servidor.tftp.recibirPaquete(recibir);	//Se recibe un paquete
			switch(servidor.tftp.catalogarPaquete(recibido))
			{
			case 1:	//RRQ
				servidor.archivo=servidor.tftp.desempaquetarRRQyWRQ(recibido);
				servidor.enviarArchivo(servidor.archivo);

			case 2:	//WRQ
				servidor.archivo=servidor.tftp.desempaquetarRRQyWRQ(recibido);
				servidor.recibirArchivo(servidor.archivo);

			case 5:	//Error
				System.out.println(servidor.tftp.desempaquetarError(recibido));

			}
			if(servidor.tftp.catalogarPaquete(recibido)!=4);	//Si no se recibe un ACK esperar - timeouts
		}
		if(acabado==true)
			servidor.tftp.cerrarConexion();
	}
	
	private void enviarArchivo(String archivo)
	{
		boolean fin=false;
		int numACK;		//Utilizar para controlar la perdida de paquetes
		int numBloq=1;	//Comienza enviando el paquete de datos con numero de bloque = 1
		byte[] aux;
		byte[] recibir=new byte[4];
		byte[] recibido;		
		byte[] paquete;
		
		while(fin!=true)
		{
			tftp.cargarArchivo(archivo);
			aux=tftp.leerBytes(numBloq);
			if(aux==null)
				fin=true;
			
			paquete=tftp.crearPaqueteData(numBloq, aux);
			if(tftp.enviarPaquete(hostCliente, puertoCliente, paquete)==false)
				System.out.println("No se ha podido realizar la operacion");	
			
			recibido = tftp.recibirPaquete(recibir);//Se recibe el paquete de respuesta
			if(tftp.catalogarPaquete(recibido)==4)
			{
				numACK=tftp.desempaquetarACK(recibido);
				if(numACK!=numBloq)		//Si el num del ACK no concuerda con el del bloque, terminar
					fin=true;
			}

			numBloq++;
			
			if(tftp.catalogarPaquete(recibido)==5)	//Si es un error muestra el mensaje
			{	
				System.out.println(tftp.desempaquetarError(recibido));		
				fin=true;
			}
		}
	}
	
	private void recibirArchivo(String archivo)
	{
		boolean fin=false;
		int numBloq=0;
		byte[] recibir=new byte[4];
		byte[] paquete;
		
		paquete= tftp.crearPaqueteACK(numBloq);	//Se envia un ACK 0		
		if(tftp.enviarPaquete(hostCliente, puertoCliente, paquete)==false)	
			System.out.println("No se ha podido realizar la operacion");			

		while(fin!=true)
		{
			paquete = tftp.recibirPaquete(recibir);//Se recibe el paquete de respuesta
			if(tftp.catalogarPaquete(paquete)==4)	//Si es un ACK
			{
				tftp.crearArchivo(archivo);
				if(tftp.escribirBytes(paquete,numBloq)!=true)
					fin=true;

				paquete=tftp.crearPaqueteACK(numBloq);
				numBloq++;
				if(tftp.enviarPaquete(hostCliente, puertoCliente, paquete)==false)	
					System.out.println("No se ha podido realizar la operacion");	
			}
			if(tftp.catalogarPaquete(recibir)==5)			
			{	
				System.out.println(tftp.desempaquetarError(recibir));		
				fin=true;
			}
		}	
	}
}
