//
//  DetalhesDadosTabela.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 02/10/17.
//   
//

import UIKit

class DetalhesDadosTabela{
    
    var letreiro: String!
    var operacao: Int!
    var ida: String!
    var volta: String!
    
    init(letreiro: String, operacao: Int, ida: String, volta: String){
        self.letreiro = letreiro
        self.operacao = operacao
        self.ida = ida
        self.volta = volta
    }
    
}
