package com.jagielu.groovy.wslite2

import com.jagielu.groovy.wslite.InfrastructureException
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import wslite.http.HTTPClientException
import wslite.http.HTTPRequest
import wslite.http.HTTPResponse
import wslite.http.auth.HTTPBasicAuthorization
import wslite.soap.SOAPClient
import wslite.soap.SOAPMessageBuilder
import wslite.soap.SOAPResponse

import javax.xml.ws.soap.SOAPFaultException


class examplewslites {

    private static Log log = LogFactory.getLog(examplewslites)

    SOAPClient client = new SOAPClient('http://192.168.0.101:7001/ServicioSumarImpl/ServicioSumarImplService?WSDL')

    def getSumaTotal(int _num1, int _num2) {
       // client.authorization=new HTTPBasicAuthorization("username","password")
        // def response = new SOAPMessageBuilder().build({
        def response = send('http://192.168.0.101:7001/ServicioSumarImpl/ServicioSumarImplService/') {
            body {
                sumar(xmlns: "http://beans.sumaws.pe.hermes.com/") {
                    arg0(_num1)
                    arg1(_num2)
                }
            }
        }
        //)
        //  def message = client.send(response.toString());
        //  message.sumarResponse.return.text()
        response.sumarResponse.return.text()
    }


    def send(String action, Closure cl) {
        withExceptionHandler {
            sendWithLogging(action, cl)
        }
    }
    private SOAPResponse sendWithLogging(String action, Closure cl) {
        SOAPResponse response = client.send(SOAPAction: action, cl)
        log(response?.httpRequest, response?.httpResponse)
        return response
    }

    private SOAPResponse withExceptionHandler(Closure cl) {
        try {
            cl.call()
        } catch (SOAPFaultException soapEx) {
            log(soapEx.httpRequest, soapEx.httpResponse)
            def message = soapEx.hasFault() ? soapEx.fault.text() : soapEx.message
            throw new InfrastructureException(message)
        } catch (HTTPClientException httpEx) {
            log(httpEx.request, httpEx.response)
            throw new InfrastructureException(httpEx.message)
        }
    }
    private void log(HTTPRequest request, HTTPResponse response) {
        log.debug("HTTPRequest $request with content:\n${request?.contentAsString}")
        log.debug("HTTPResponse $response with content:\n${response?.contentAsString}")
    }
}
static void main(String[] args) {

    def services= new examplewslites()

    println services.getSumaTotal(1,2)

}