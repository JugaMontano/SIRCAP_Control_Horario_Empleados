SIRCAP - Sistema Integrado para Registro y Control de Asistencia del Personal
=============================================================================

CÓMO EJECUTARLO
---------------
Opción A (más fácil): doble clic en SIRCAP.jar  (necesita Java 11 o superior)
Opción B: ejecutar.bat (Windows) o ./ejecutar.sh (Linux/Mac). Compila y abre la app.
Opción C: IntelliJ IDEA
  1. File > New > Project from Existing Sources... y elige esta carpeta
     (o crea un proyecto Java y copia la carpeta "src" dentro).
  2. Abre src/sircap/Main.java y pulsa el triángulo verde junto a "main".

PRIMER INGRESO
--------------
Usuario: admin      Contraseña: admin123
Cámbiala con el botón "Cambiar contraseña" (arriba a la derecha).
Luego, en la pestaña "Empleados", registra a las personas de la empresa.

DÓNDE SE GUARDAN LOS DATOS
--------------------------
En la carpeta "datos" (archivos .csv), que se crea sola junto a donde ejecutes la app.
Para hacer una copia de seguridad, copia esa carpeta. Para empezar de cero, bórrala.

QUÉ PUEDE HACER CADA ROL
------------------------
Empleado        : marcar entrada/salida, ver su historial, enviar solicitudes
Recursos Humanos: lo anterior + empleados, horarios, ausencias, aprobar solicitudes
                  y horas extras, reportes
Gerencia        : marcar asistencia, solicitudes, horarios, reportes, panel gerencial
Administrador   : todo, incluida la auditoría

ESTRUCTURA DEL CÓDIGO
---------------------
src/sircap/               Modelo y reglas del negocio
  SistemaSIRCAP.java      Controlador: permisos, validaciones, auditoría
  Almacen.java            Guarda/carga los datos en CSV (único lugar que habría que
                          cambiar para usar una base de datos como SQLite)
src/sircap/ui/            Pantallas (Java Swing)
  Main.java (en sircap/)  Punto de entrada
