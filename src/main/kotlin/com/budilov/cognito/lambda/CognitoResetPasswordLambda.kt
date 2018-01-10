package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.cognito.CognitoService
import com.google.gson.Gson

class CognitoResetPasswordLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {


    val cognito = CognitoService()

    /**
     * 1. Get the request from API Gateway. Unmarshal (automatically) the request
     * 2. Get the
     */
    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse? {

        val logger = context?.logger

        val idToken = request?.headers?.get("idToken")

        var status = 400
        var response: String = ""

        if (idToken != null) {
            // Check to see if the token is valid and if the username matches the
            // idToken's username
            val tokenValid = try {
                cognito.isTokenValid(idToken)
            } catch (e: Exception) {
                logger?.log("Couldn't figure out if the id token is valid...caught an exception...${e.stackTrace}")
                false
            }

            if (tokenValid) {
                val username = cognito.getUsername(idToken)
                response = Gson().toJson(cognito.adminResetPassword(username = username))
                status = 200
            }

        } else {
            logger?.log("The id token is required")
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}