package utec.idontknowbackend.polymarket;

import utec.idontknowbackend.mercado.model.Mercado;

import java.math.BigDecimal;

public record MovimientoMercado(Mercado mercado, BigDecimal anterior, BigDecimal nueva) {}
