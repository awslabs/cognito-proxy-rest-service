package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoUpdateUserAttributeLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {

    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse {
        val logger = context?.logger

        val idToken = request?.headers?.get("idToken")
        val attributeName = request?.headers?.get("attributeName")
        val attributeValue = request?.headers?.get("attributeValue")

        var status = 400
        var response = ""

        if (idToken != null && attributeName != null
                && attributeValue != null) {
            // Check to see if the token is valid and if the username matches the
            // idToken's username

            try {
                if (cognito.isTokenValid(idToken)) {
                    val username = cognito.getUsername(idToken)
                    response = Gson().toJson(cognito.updateUserAttribute(username = username,
                            attributeName = attributeName, attributeValue = attributeValue))
                    status = 200
                }
            } catch (e: Exception) {
                logger?.log("Couldn't figure out if the id token is valid...caught an exception...${e.stackTrace}")
                status = 400
            }

        } else {
            return ApiGatewayResponse(statusCode = 400, body = "A valid id token is required")
        }

        return ApiGatewayResponse(statusCode = status, body = response)

    }
}