package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.cognito.CognitoService
import com.google.gson.Gson

class CognitoDeleteUserLambda : RequestHandler<ApiGatewayRequest.Input,
        CognitoDeleteUserLambda.AuthResponse> {

    data class AuthResponse(val statusCode: Int,
                            val body: String)

    val cognito = CognitoService()

    /**
     * 1. Get the request from API Gateway. Unmarshal (automatically) the request
     * 2. Get the
     */
    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): AuthResponse? {

        val logger = context?.logger

        val idToken = request?.headers?.get("idToken")

        var status = 400
        val resultBody = if (idToken != null) {


            val tokenValid = cognito.isTokenValid(idToken)

            var deleteUserResponse: Any = "Couldn't delete the user because of a bad token."
            if (tokenValid) {
                status = 200
                deleteUserResponse = cognito.adminDeleteUser(username = cognito.getUsername(idToken))
            }

            Gson().toJson(deleteUserResponse)
        } else {
            logger?.log("Username and idToken are required")
            "Username and idToken are required"
        }

        logger?.log("request payload: " + Gson().toJson(request))

        return AuthResponse(status, resultBody)
    }
}