package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoDeleteUserLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {


    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse? {

        val logger = context?.logger

        val idToken = request?.headers?.get("idToken")

        var status = 400
        var response = ""

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
                response = Gson().toJson(cognito.adminDeleteUser(username = username))
                status = 200
            }

        } else {
            logger?.log("The id token is required")
        }

        return ApiGatewayResponse(statusCode = status, body = Gson().toJson(response))
    }
}