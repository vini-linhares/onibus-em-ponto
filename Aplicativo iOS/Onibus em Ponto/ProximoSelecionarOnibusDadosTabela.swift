//
//  ProximoSelecionarOnibusDadosTabela.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 03/10/17.
//   
//

import UIKit

class ProximoSelecionarOnibusDadosTabela{
    var distValor: Int!
    var distText: String!
    var end: String!
    var linhaNum: String!
    var linhaNome: String!
    
    init(distValor: Int, distText: String, end: String, linhaNum: String, linhaNome: String){
        self.distValor = distValor
        self.distText = distText
        self.end = end
        self.linhaNum = linhaNum
        self.linhaNome = linhaNome
    }
}
