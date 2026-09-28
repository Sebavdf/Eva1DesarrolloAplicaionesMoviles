package org.example
class CodigoInvalidoException(mensaje: String) : Exception(mensaje)
class TarifaInvalidaException(mensaje: String) : Exception(mensaje)
class PacienteNoEncontradoException(mensaje: String) : Exception(mensaje)
class SistemaSinCapacidadException(mensaje: String) : Exception(mensaje)