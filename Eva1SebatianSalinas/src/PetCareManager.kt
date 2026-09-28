package org.example

class PetCareManager(val capacidad: Int = 10) {
    val boxes = Array(capacidad) { Box(it + 1) }
    private val historial = mutableListOf<Ticket>()
    private var contadorTickets = 1

    // R5: Simulación de entrada (3 segundos)
    fun registrarEntrada(paciente: Paciente) {
        val boxLibre = boxes.firstOrNull { it.estado is EstadoBox.Libre }
            ?: throw SistemaSinCapacidadException("No hay boxes disponibles en este momento.")

        boxLibre.estado = EstadoBox.EnProceso("Esperando confirmacion de sensor de entrada...")
        Thread.sleep(3000)
        boxLibre.estado = EstadoBox.EnAtencion(paciente)

        println("[OK - ENTRADA] ${paciente.obtenerDetalle()} ingresado al Box #${boxLibre.numero}.")
    }

    // R5: Simulación de salida (6.5 segundos)
    fun registrarSalida(codigo: String, minutosUso: Long) {
        val box = boxes.firstOrNull {
            val estado = it.estado
            estado is EstadoBox.EnAtencion && estado.paciente.codigo == codigo
        } ?: throw PacienteNoEncontradoException("No se encontro un paciente activo con el codigo: $codigo")

        val paciente = (box.estado as EstadoBox.EnAtencion).paciente
        box.estado = EstadoBox.EnProceso("Procesando salida y calculando tarifa...")
        Thread.sleep(6500)

        // Polimorfismo puro: no importa la clase concreta, se ejecuta la regla del paciente
        val montoFinal = paciente.calcularTarifaFinal(minutosUso)

        // R3 y R6: Validación de tarifa cero o negativa
        if (montoFinal <= 0.0 && !(paciente is Felino && minutosUso < 20)) {
            box.estado = EstadoBox.EnAtencion(paciente)
            throw TarifaInvalidaException("Error de cobro: la tarifa calculada ($montoFinal) no es valida.")
        }

        val ticket = Ticket(contadorTickets++, paciente, minutosUso, montoFinal)
        historial.add(ticket)
        box.estado = EstadoBox.Libre

        println("[OK - SALIDA] Ticket #${ticket.numero} emitido para ${paciente.nombre}. Total pagado: $${montoFinal.toLong()}")
    }

    // R4: Consultas de Negocio
    fun boxesDisponibles(): Int = boxes.count { it.estado is EstadoBox.Libre }

    fun pacientesConvenio(): List<Paciente> =
        historial.filter { it.paciente.tipoDueno == TipoDueno.CONVENIO }.map { it.paciente }

    fun ingresoPromedio(): Double =
        if (historial.isEmpty()) 0.0 else historial.map { it.montoCobrado }.average()

    fun codigosFinalizados(): List<String> =
        historial.map { it.paciente.codigo }

    fun pacienteMayorTiempo(): Paciente? =
        historial.maxByOrNull { it.minutosUso }?.paciente

    // Reporte de cierre de turno
    fun generarReporteCierre() {
        println("\n================================================================================")
        println("                           REPORTE DE CIERRE DE TURNO                           ")
        println("================================================================================")
        println(String.format("%-8s | %-30s | %-8s | %-8s | %-10s", "TICKET", "PACIENTE", "CODIGO", "TIEMPO", "TOTAL"))
        println("-".repeat(80))

        var totalRecaudado = 0.0
        val recaudacionPorTipo = mutableMapOf("Canino" to 0.0, "Felino" to 0.0, "Exotico" to 0.0)

        historial.forEach { t ->
            totalRecaudado += t.montoCobrado
            val tipo = t.paciente.javaClass.simpleName
            recaudacionPorTipo[tipo] = (recaudacionPorTipo[tipo] ?: 0.0) + t.montoCobrado

            println(String.format(
                "%-8s | %-30s | %-8s | %-5d min | $%-10d",
                "#${t.numero}",
                t.paciente.obtenerDetalle(),
                t.paciente.codigo,
                t.minutosUso,
                t.montoCobrado.toLong()
            ))
        }

        println("-".repeat(80))
        val tipoMasIngresos = recaudacionPorTipo.maxByOrNull { it.value }?.key ?: "N/A"

        println("Resumen General:")
        println("- Total recaudado: $${totalRecaudado.toLong()}")
        println("- Pacientes atendidos: ${historial.size}")
        println("- Ingreso promedio: $${ingresoPromedio().toLong()}")
        println("- Tipo de paciente con mas ingresos: $tipoMasIngresos")
        println("- Boxes disponibles al cierre: ${boxesDisponibles()}")
        println("================================================================================")
    }
}