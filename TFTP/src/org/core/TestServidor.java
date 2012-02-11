package org.core;

public class TestServidor {

	String ruta;
	String hostCliente;
	TFTP tftp;

	private TestServidor()
	{
		tftp = new TFTP(69,ruta);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestServidor servidor = new TestServidor();
		boolean fin=false;
		byte[] recibir=new byte[4];
		byte[] recibido;

		while(fin!=true)
		{
			recibido = servidor.tftp.recibirPaquete(recibir);	//Se recibe un paquete
			switch(servidor.tftp.catalogarPaquete(recibido))
			{
			case 1:	//RRQ
				

			case 2:	//WRQ


			case 5:	//Error
				System.out.println(servidor.tftp.desempaquetarError(recibido));

			}
			if(servidor.tftp.catalogarPaquete(recibido)!=4);	//Si no se recibe un ACK esperar - timeouts
		}
	}

}
