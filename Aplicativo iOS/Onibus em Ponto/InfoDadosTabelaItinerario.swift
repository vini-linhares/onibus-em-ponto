//
//  InfoDadosTabelaItinerario.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 02/10/17.
//   
//
import UIKit

class InfoDadosTabelaItinerario{
    
    var rua: String!
    var numeroInicio: Int!
    var numeroFim: Int!
    
    init(rua: String, numeroInicio: Int, numeroFim: Int){
        self.rua = rua
        self.numeroInicio = numeroInicio
        self.numeroFim = numeroFim
    }
    
}
