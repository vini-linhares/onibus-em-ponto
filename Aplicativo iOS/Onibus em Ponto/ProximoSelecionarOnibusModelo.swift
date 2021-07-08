//
//  ProximoSelecionarOnibusModelo.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 04/10/17.
//   
//

import UIKit

class ProximoSelecionarOnibusModelo{
    
    var linha: String!
    var operacao: String!
    var sentido: String!
    var nome: String!
    var latlng: String!
    
    init(linha: String, operacao: String, sentido: String, nome: String, latlng: String){
        self.linha = linha
        self.operacao = operacao
        self.sentido = sentido
        self.nome = nome
        self.latlng = latlng
    }
    
}

