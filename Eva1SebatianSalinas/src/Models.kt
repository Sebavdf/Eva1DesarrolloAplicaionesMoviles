package org.example

import java.time.LocalDateTime
import kotlin.math.round

enum class TipoDueno { PARTICULAR, CONVENIO, MUNICIPAL }

// Clase base abstracta (Herencia)
abstract class Paciente(
    val codigo: String,
    val nombre: String,
    val especie: String,
    val ingreso: LocalDateTime,
    val tipoDueno: TipoDueno
) {
    init {
        // R3: Validación de formato de código (ej: CA12CD)
        if (!codigo.matches(Regex("^[a-zA-Z]{2}\\d{2}[a-zA-Z]{2}$"))) {
            throw CodigoInvalidoException("El codigo '$codigo' no cumple con el formato requerido (dos letras, dos digitos, dos letras).")
        }
    }

    // Método polimórfico: cada subclase calcula su base según sus reglas
    protected abstract fun calcularCostoBase(minutos: Long): Double

    // Template method: calcula el total aplicando IVA y descuento municipal
    fun calcularTarifaFinal(minutos: Long): Double {
        val base = calcularCostoBase(minutos)
        if (base == 0.0) return 0.0

        var conIva = base * 1.19
        if (tipoDueno == TipoDueno.MUNICIPAL) {
            conIva *= 0.50
        }
        return round(conIva)
    }

    abstract fun obtenerDetalle(): String
}

// Subclase Canino: Tarifa $12.000/hr, 20% descuento si es convenio
class Canino(
    codigo: String,
    nombre: String,
    especie: String,
    ingreso: LocalDateTime,
    tipoDueno: TipoDueno
) : Paciente(codigo, nombre, especie, ingreso, tipoDueno) {

    override fun calcularCostoBase(minutos: Long): Double {
        val horas = minutos / 60.0
        var total = horas * 12000.0
        if (tipoDueno == TipoDueno.CONVENIO) {
            total *= 0.80
        }
        return total
    }

    override fun obtenerDetalle(): String = "$nombre (Canino - $especie)"
}

// Subclase Felino: Tarifa $9.000/hr, cobro $0 si atencion < 20 min
class Felino(
    codigo: String,
    nombre: String,
    especie: String,
    ingreso: LocalDateTime,
    tipoDueno: TipoDueno
) : Paciente(codigo, nombre, especie, ingreso, tipoDueno) {

    override fun calcularCostoBase(minutos: Long): Double {
        if (minutos < 20) return 0.0
        val horas = minutos / 60.0
        return horas * 9000.0
    }

    override fun obtenerDetalle(): String = "$nombre (Felino - $especie)"
}

// Subclase Exotico: Tarifa $20.000/hr, recargo 30% si es silvestre
class Exotico(
    codigo: String,
    nombre: String,
    especie: String,
    ingreso: LocalDateTime,
    tipoDueno: TipoDueno,
    val esSilvestre: Boolean
) : Paciente(codigo, nombre, especie, ingreso, tipoDueno) {

    override fun calcularCostoBase(minutos: Long): Double {
        val horas = minutos / 60.0
        var total = horas * 20000.0
        if (esSilvestre) {
            total *= 1.30
        }
        return total
    }

    override fun obtenerDetalle(): String {
        val silvestreTexto = if (esSilvestre) "Silvestre: SI" else "Silvestre: NO"
        return "$nombre (Exotico - $especie | $silvestreTexto)"
    }
}

// R2: Estados de Boxes
sealed class EstadoBox {
    object Libre : EstadoBox()
    data class EnAtencion(val paciente: Paciente) : EstadoBox()
    data class EnProceso(val motivo: String) : EstadoBox()
    data class FueraDeServicio(val motivo: String) : EstadoBox()
}

data class Box(val numero: Int, var estado: EstadoBox = EstadoBox.Libre)

data class Ticket(
    val numero: Int,
    val paciente: Paciente,
    val minutosUso: Long,
    val montoCobrado: Double
)