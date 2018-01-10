package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.cognito.CognitoService
import com.google.gson.Gson

class CognitoTokenValidLambda : RequestHandler<ApiGatewayRequest.Input,
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
            response = Gson().toJson(cognito.isTokenValid(idToken))
            status = 200

        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}