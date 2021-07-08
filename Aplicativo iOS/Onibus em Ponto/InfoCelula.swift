//
//  InfoCelula.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 02/10/17.
//   
//

import UIKit

class InfoCelula: UITableViewCell {
    
    @IBOutlet weak var lblRua: UILabel!
    @IBOutlet weak var lblNum: UILabel!
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}

