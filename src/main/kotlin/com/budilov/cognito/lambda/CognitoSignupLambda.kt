package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.cognito.CognitoService
import com.google.gson.Gson

class CognitoSignupLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {

    val cognito = CognitoService()

    /**
     * 1. Get the request from API Gateway. Unmarshal (automatically) the request
     * 2. Get the
     */
    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse {
        val logger = context?.logger

        val username = request?.headers?.get("username")
        val password = request?.headers?.get("password")

        logger?.log("${username} & ${password}")
        var status = 400
        var response: String = ""

        if (username != null && password != null) {
            status = 200
            response = Gson().toJson(cognito.signUp(username = username,
                    password = password))
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}