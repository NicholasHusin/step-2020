package com.google.sps.utils;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;


/**
 * Language class intended to be used as a utility class.
 * Provides common functions related to language such as translation
 **/
public class Language {
  public static String translate(String originalText, String languageCode) {
    Translate translate = TranslateOptions.getDefaultInstance().getService();
    Translation translation =
      translate.translate(originalText, Translate.TranslateOption.targetLanguage(languageCode));

    return translation.getTranslatedText();
  }
}
