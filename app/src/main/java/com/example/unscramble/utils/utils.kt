package com.example.unscramble.utils

import android.content.Context
import java.util.Locale

fun getCurrentLanguage(context: Context): String {
    return context.resources.configuration.locales.get(0).language
}