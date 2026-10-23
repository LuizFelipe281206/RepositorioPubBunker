package com.pubbunker.service;

import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class ConflitoBanco {
    private ConflitoBanco() { }

    public static boolean nomeUnico(Throwable exception, String indice) {
        Set<Throwable> visitadas = Collections.newSetFromMap(
                new IdentityHashMap<>()
        );
        ArrayDeque<Throwable> pendentes = new ArrayDeque<>();
        pendentes.add(exception);

        while (!pendentes.isEmpty()) {
            Throwable causa = pendentes.removeFirst();

            if (!visitadas.add(causa)) {
                continue;
            }

            if (
                    causa instanceof ConstraintViolationException violacao
                            && "23505".equals(violacao.getSQLState())
                            && indice.equals(violacao.getConstraintName())
            ) {
                return true;
            }

            if (
                    causa instanceof PSQLException postgres
                            && "23505".equals(postgres.getSQLState())
                            && postgres.getServerErrorMessage() != null
                            && indice.equals(
                            postgres.getServerErrorMessage().getConstraint()
                    )
            ) {
                return true;
            }

            if (causa.getCause() != null) {
                pendentes.addLast(causa.getCause());
            }

            if (
                    causa instanceof SQLException sql
                            && sql.getNextException() != null
            ) {
                pendentes.addLast(sql.getNextException());
            }
        }

        return false;
    }

}
