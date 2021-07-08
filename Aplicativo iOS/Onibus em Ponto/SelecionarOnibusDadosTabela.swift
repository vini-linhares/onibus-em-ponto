//
//  SelecionarOnibusDadosTabela.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 03/10/17.
//   
//

import UIKit

class SelecionarOnibusDadosTabela{
    
    var numero: String!
    var nome: String!
    var operacao: Int!
    var sentido: Int!
    var selecionado: Bool!
    
    init(numero: String, nome: String, operacao: Int, sentido: Int){
        self.numero = numero
        self.nome = nome
        self.operacao = operacao
        self.sentido = sentido
        self.selecionado = false
    }
}
