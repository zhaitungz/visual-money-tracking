package com.example.visualmoneytracker.domain.model

sealed class CloudProvider {
    object GoogleDrive : CloudProvider()
    object Box : CloudProvider()
}
