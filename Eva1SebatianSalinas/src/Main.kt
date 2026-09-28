package org.example

import java.time.LocalDateTime

fun main() {
    val sistema = PetCareManager()
    val ahora = LocalDateTime.now()

    println("Iniciando sistema PetCare SpA...")
    println("Capacidad inicial de boxes: ${sistema.boxesDisponibles()}")
    println("--------------------------------------------------------------------------------")

    // R6: Funciones de resguardo para que los errores no boten la app
    fun intentarEntrada(creador: () -> Paciente) {
        try {
            val paciente = creador()
            sistema.registrarEntrada(paciente)
        } catch (e: Exception) {
            println("[ERROR - ENTRADA] ${e.message}")
        }
    }

    fun intentarSalida(codigo: String, minutos: Long) {
        try {
            sistema.registrarSalida(codigo, minutos)
        } catch (e: Exception) {
            println("[ERROR - SALIDA] ($codigo): ${e.message}")
        }
    }

    // 1. Registro de entradas con datos de prueba
    intentarEntrada { Canino("CA12CD", "Max", "Golden Retriever", ahora, TipoDueno.CONVENIO) }
    intentarEntrada { Canino("CA99ZA", "Luna", "Labrador", ahora, TipoDueno.PARTICULAR) }
    intentarEntrada { Felino("FE22TO", "Misi", "Siames", ahora, TipoDueno.PARTICULAR) }
    intentarEntrada { Exotico("EX44RG", "Loro", "Amazonico", ahora, TipoDueno.MUNICIPAL, esSilvestre = true) }
    intentarEntrada { Exotico("EX77RG", "Iguana", "Verde", ahora, TipoDueno.PARTICULAR, esSilvestre = false) }

    // Prueba de error R6: Código inválido
    intentarEntrada { Canino("123ABC", "ErrorDog", "Mestizo", ahora, TipoDueno.PARTICULAR) }

    println("\nProcesando salidas de pacientes...")
    println("--------------------------------------------------------------------------------")

    // 2. Registro de salidas con tiempos de prueba
    intentarSalida("CA12CD", 75)
    intentarSalida("CA99ZA", 180)
    intentarSalida("FE22TO", 18)   // Felino < 20 min -> $0
    intentarSalida("EX44RG", 120)
    intentarSalida("EX77RG", 45)

    // Prueba de error R6: Paciente no encontrado
    intentarSalida("XX99ZZ", 50)

    // 3. Consultas de negocio (R4)
    println("\nConsultas del sistema:")
    println("- Boxes libres actuales: ${sistema.boxesDisponibles()}")
    println("- Pacientes de convenio atendidos: ${sistema.pacientesConvenio().map { it.nombre }}")
    println("- Codigos finalizados: ${sistema.codigosFinalizados()}")
    println("- Paciente con mayor tiempo: ${sistema.pacienteMayorTiempo()?.nombre ?: "N/A"}")

    // 4. Reporte de cierre
    sistema.generarReporteCierre()
}