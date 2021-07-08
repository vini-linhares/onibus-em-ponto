//
//  MeuOnibusCelula.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 03/10/17.
//   
//

import UIKit

class MeuOnibusCelula: UITableViewCell {
    
    
    @IBOutlet weak var imgMiniLinha: UIImageView!
    @IBOutlet weak var txtLinha: UILabel!
    @IBOutlet weak var txtNome: UILabel!
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}

