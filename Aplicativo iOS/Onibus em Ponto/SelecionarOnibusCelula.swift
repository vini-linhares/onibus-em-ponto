//
//  SelecionarOnibusCelula.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 03/10/17.
//   
//

import UIKit

class SelecionarOnibusCelula: UITableViewCell {
    
    
    @IBOutlet weak var imgMinilinha: UIImageView!
    @IBOutlet weak var lblLinha: UILabel!
    @IBOutlet weak var lblNome: UILabel!
    @IBOutlet weak var lblFundo: UILabel!
    
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}
