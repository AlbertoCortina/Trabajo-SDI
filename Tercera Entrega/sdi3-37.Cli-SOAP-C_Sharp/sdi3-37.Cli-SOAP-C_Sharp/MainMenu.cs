﻿using System;
namespace sdi3_37.Cli_C_Sharp
{
    class MainMenu
    {

        static void Main(string[] args)
        {          
            //Selecionar opción
            while(true)
            {
                Console.WriteLine("ADMINISTRADOR");

                //Opción 1: ListarUsuariosAction            
                Console.Write("\t1- Listar usuarios\n");

                //Opción 2: DeshabilitarUsuarioAction
                Console.Write("\t2- Deshabilitar usuario\n");

                //Opción 3: EliminarUsuarioAction
                Console.Write("\t3- Eliminar usuario\n");

                Console.Write("Opción:");
                switch (Console.ReadLine())
                {
                    case "0":
                        return;
                    case "1":
                        Console.WriteLine();
                        new ListarUsuariosAction().execute();
                        break;
                    case "2":
                        Console.WriteLine();
                        new DeshabilitarUsuarioAction().execute();
                        break;
                    case "3":
                        Console.WriteLine();
                        new EliminarUsuarioAction().execute();
                        break;
                    default:
                        Console.WriteLine("\tOpción errónea");
                        break;
                }
                Console.WriteLine();
            }           
        }
    }
}
