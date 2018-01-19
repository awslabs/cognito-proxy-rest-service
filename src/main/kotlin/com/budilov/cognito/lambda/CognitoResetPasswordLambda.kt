package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoResetPasswordLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {


    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse? {

        val logger = context?.logger

        val idToken = request?.headers?.get("idToken")

        var status = 200
        var response = ""

        if (idToken != null) {
            // Check to see if the token is valid and if the username matches the
            // idToken's username
            try {
                if (cognito.isTokenValid(idToken)) {
                    val username = cognito.getUsername(idToken)
                    response = Gson().toJson(cognito.adminResetPassword(username = username))
                }
            } catch (e: Exception) {
                logger?.log("Couldn't figure out if the id token is valid...caught an exception...${e.message}")
                status = 400
            }

        } else {
            logger?.log("The id token is required")
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}