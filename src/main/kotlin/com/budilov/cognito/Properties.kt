package com.budilov.cognito

import com.google.gson.GsonBuilder

/**
 * Created by Vladimir Budilov
 * https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json -->
 *
 * https://cognito-idp.us-east-1.amazonaws.com/us-east-1_PGSbCVZ7S/.well-known/jwks.json
 */
data class Properties(val regionName: String, val cognitoUserPoolId: String,
                      val cognitoClientId: String, val jwtIdTokenKid: String)

object PropertyFileConverter {

    internal fun readCredentials(): Properties {
        val gson = GsonBuilder().create()
        return gson.fromJson(PropertyFileConverter.javaClass.classLoader.getResource("properties.json").readText(), Properties::class.java)
    }
}