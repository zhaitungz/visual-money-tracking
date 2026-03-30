package com.example.visualmoneytracker.domain.model

// Feature: visual-money-tracker, Property 1: Transaction type invariant

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.property.checkAll
import io.kotest.property.arbitrary.enum

/**
 * Property 1: Transaction type invariant
 * Validates: Requirements 2.1.1
 *
 * For any TransactionType value, it must be exactly one of INCOME or EXPENSE.
 */
class TransactionTypePropertyTest : FreeSpec({

    "Transaction type invariant" - {
        "any generated TransactionType must be either INCOME or EXPENSE" {
            checkAll(100, enum<TransactionType>()) { type ->
                (type == TransactionType.INCOME || type == TransactionType.EXPENSE).shouldBeTrue()
            }
        }
    }
})
