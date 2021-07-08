//
//  InfoViewController.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 02/10/17.
//   
//

import UIKit
import AVFoundation // Audio
import Speech       //Ouvir

class InfoViewController: UIViewController, UITableViewDataSource, AVAudioPlayerDelegate{
    
    
    var speechTimer = Timer()
    @objc public func updateCounterSpeech() {
        speechVerificar()
    }
    func speechVerificar(){
        if self.comando == self.comandoAntigo{
            print("Igual - Parou")
            pararOuvir()
            speechTimer.invalidate()
        }else{
            self.comandoAntigo = self.comando
            print("DIFERENTE - CONTINUA")
        }
    }
    
    
    
    @IBOutlet weak var UITableView: UITableView!
    @IBOutlet weak var lblLinha: UILabel!
    @IBOutlet weak var lblIda: UILabel!
    @IBOutlet weak var lblVolta: UILabel!
    @IBOutlet weak var lblUtilInicio: UILabel!
    @IBOutlet weak var lblSabadoInicio: UILabel!
    @IBOutlet weak var lblDomingoInicio: UILabel!
    @IBOutlet weak var lblUtilFim: UILabel!
    @IBOutlet weak var lblSabadoFim: UILabel!
    @IBOutlet weak var lblDomingoFim: UILabel!
    @IBOutlet weak var btnDireita: UIButton!
    
    var linha : String!
    
    
    var data: [InfoDadosTabelaItinerario] = []
    
    
    
    //Falar
    let synth = AVSpeechSynthesizer()
    var myUtterance = AVSpeechUtterance(string: "")
    //var falando = false
    var ruaAnterior = ""
    var numAnterior = ""
    var isVoice = false
    var salvaVoice = "salvaVoice"
    var destinoIdentificado = false
    var mensagemInicio = true
    var falarDistancia = false
    var linhaPorFala = -1
    
    //Ouvir
    enum SpeechStatus {
        case ready
        case recognizing
        case unavailable
    }
    var status = SpeechStatus.ready
    let audioEngine = AVAudioEngine()
    let speechRecognizer: SFSpeechRecognizer? = SFSpeechRecognizer(locale: Locale(identifier: "pt-BR"))
    let request = SFSpeechAudioBufferRecognitionRequest()
    var recognitionTask: SFSpeechRecognitionTask?
    var comando = ""
    var comandoAntigo = ""
    var player = AVAudioPlayer()
    
    var continuar = false
    var comandoVoz = false
    var itinerario = ""
    var horarios = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        UITableView.dataSource = self
        
        UserDefaults.standard.set("s", forKey: "Voltando")
        
        if getVoice() == "s"{
            //Botão
            let screenSize: CGRect = UIScreen.main.bounds
            btnDireita.frame = CGRect(x: screenSize.width * 0.5, y: 0, width: screenSize.width * 0.5, height: screenSize.height)
            btnDireita.imageEdgeInsets = UIEdgeInsetsMake(btnDireita.frame.size.height, btnDireita.frame.size.width, btnDireita.frame.size.height, btnDireita.frame.size.width)
            print(String(describing: screenSize.width) + "-" + String(describing: screenSize.width))
            //Ouvir
            switch SFSpeechRecognizer.authorizationStatus() {
            case .notDetermined:
                askSpeechPermission()
            case .authorized:
                self.status = .ready
            case .denied, .restricted:
                self.status = .unavailable
            }
            isVoice = true
            falar(texto: "Buscando informações de linha " + self.linha)
        }else{
            btnDireita.isHidden = true
            isVoice = false
        }
        
        
        lblLinha.text = linha
        
        if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api/itinerario/index2.php?sentido=1&linha="+linha){
        //if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api/linha/?busca=1"){
            let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                if erro == nil{
                    if let dadosRetorno = dados{
                        
                        
                        do{
                            if let objetoJson = try JSONSerialization.jsonObject(with: dadosRetorno, options: []) as? [String: Any]{
                                //print(objetoJson)
                                if let ida = objetoJson["ida"]{
                                    //print(ida)
                                    if let array = ida as? [Any]{
                                        if let um = array.first{
                                            print(um)
                                            self.montarTabela(dados: array)
                                            self.continua()
                                       }
                                    }
                                }
                                
                            }
                        }catch{
                            print("Erro no Retorno")
                        }
 
                        
                    }
                    
                    
                }else{
                    print(erro ?? "Erro")
                }
            }
            tarefa.resume()
        }
        if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api/info/index2.php?&linha="+linha){
            let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                if erro == nil{
                    if let dadosRetorno = dados{
                        
                        do{
                            if let objetoJson = try JSONSerialization.jsonObject(with: dadosRetorno, options: []) as? [String: Any]{
                                print(objetoJson)
                                if let ida = objetoJson["ida"]{
                                    DispatchQueue.main.async {
                                        self.lblIda.text = ida as? String
                                    }
                                }
                                if let ida = objetoJson["volta"]{
                                    DispatchQueue.main.async {
                                        self.lblVolta.text = ida as? String
                                    }
                                }
                                if let semana = objetoJson["semana"] as? [String: Any]{
                                    DispatchQueue.main.async {
                                        self.lblUtilInicio.text = semana["inicial"] as? String
                                        self.lblUtilFim.text = semana["fim"] as? String
                                        if self.lblUtilInicio.text != "" && self.lblUtilFim.text != "-"{
                                            self.horarios += "Em dias úteis essa linha funciona no ponto inicial das "
                                            var arrHora = self.lblUtilInicio.text?.components(separatedBy: "-")
                                            self.horarios += self.formataHora(texto: arrHora![0]) + " as " + self.formataHora(texto: arrHora![1])
                                            self.horarios += " E No ponto final das "
                                            arrHora = self.lblUtilFim.text?.components(separatedBy: "-")
                                            self.horarios += self.formataHora(texto: arrHora![0]) + " as " + self.formataHora(texto: arrHora![1])
                                        }else if self.lblUtilFim.text != ""{
                                            self.horarios += "Em dias úteis essa linha funciona das "
                                            var arrHora = self.lblUtilInicio.text?.components(separatedBy: "-")
                                            self.horarios += self.formataHora(texto: arrHora![0]) + " as " + self.formataHora(texto: arrHora![1])
                                        }
                                    }
                                }
                                if let sabado = objetoJson["sabado"] as? [String: Any]{
                                    DispatchQueue.main.async {
                                        self.lblSabadoInicio.text = sabado["inicial"] as? String
                                        self.lblSabadoFim.text = sabado["fim"] as? String
                                        if self.lblSabadoInicio.text != "" && self.lblSabadoFim.text != "-"{
                                            self.horarios += ". \n Nos sábados essa linha funciona no ponto inicial das "
                                            var arrHora = self.lblSabadoInicio.text?.components(separatedBy: "-")
                                            self.horarios += self.formataHora(texto: arrHora![0]) + " as " + self.formataHora(texto: arrHora![1])
                                            self.horarios += " E No ponto final das "
                                            arrHora = self.lblSabadoFim.text?.components(separatedBy: "-")
                                            self.horarios += self.formataHora(texto: arrHora![0]) + " as " + self.formataHora(texto: arrHora![1])
                                        }else if self.lblSabadoFim.text != ""{
                                            self.horarios += ". \n Nos sábados essa linha funciona das "
                                            var arrHora = self.lblSabadoInicio.text?.components(separatedBy: "-")
                                            self.horarios += self.formataHora(texto: arrHora![0]) + " as " + self.formataHora(texto: arrHora![1])
                                        }
                                    }
                                }
                                if let domingo = objetoJson["domingo"] as? [String: Any]{
                                    DispatchQueue.main.async {
                                        self.lblDomingoInicio.text = domingo["inicial"] as? String
                                        self.lblDomingoFim.text = domingo["fim"] as? String
                                        if self.lblDomingoInicio.text != "" && self.lblDomingoFim.text != "-"{
                                            self.horarios += ". \n E nos domingos ou feriados essa linha funciona no ponto inicial das "
                                            var arrHora = self.lblDomingoInicio.text?.components(separatedBy: "-")
                                            self.horarios += self.formataHora(texto: arrHora![0]) + " as " + self.formataHora(texto: arrHora![1])
                                            self.horarios += " E No ponto final das "
                                            arrHora = self.lblDomingoFim.text?.components(separatedBy: "-")
                                            self.horarios += self.formataHora(texto: arrHora![0]) + " as " + self.formataHora(texto: arrHora![1])
                                        }else if self.lblDomingoFim.text != ""{
                                            self.horarios += ". \n E nos domingos ou feriados essa linha funciona das "
                                            var arrHora = self.lblDomingoInicio.text?.components(separatedBy: "-")
                                            self.horarios += self.formataHora(texto: arrHora![0]) + " as " + self.formataHora(texto: arrHora![1])
                                        }
                                    }
                                }
                                self.continua()
                            }
                        }catch{
                            print("Erro no Retorno")
                        }
                    }
                }else{
                    print("erro")
                }
            }
            tarefa.resume()
        }
    }
    
    func formataHora(texto: String) -> String{
        var txt = ""
        var arrTxt = texto.components(separatedBy: ":")
        
        if arrTxt[0] == "00"{
            txt = "Meia noite"
        }else{
            txt = String(describing: Int(arrTxt[0])!)
        }
        
        if arrTxt[1] == "00"{
            if arrTxt[0] != "00" && arrTxt[0] != "01"{
                txt += " horas"
            }else if arrTxt[0] != "01"{
                txt += " hora"
            }
        }else if arrTxt[1] == "30"{
            txt += " e meia"
        }else{
            txt += " e " + String(describing: Int(arrTxt[1])!)
        }
        
        return txt
    }
    
    func continua(){
        DispatchQueue.main.async {
            if self.continuar{
                self.falar(texto: "Linha de " + self.trocaAbrev(texto: self.lblIda.text!)  + " a " + self.trocaAbrev(texto: self.lblVolta.text!) +  ". Para mais informações diga: \n Horários de operação, ou, \n Itinerário")
                self.comandoVoz = true
            }else{
                self.continuar = true
            }
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        synth.stopSpeaking(at: AVSpeechBoundary.immediate)
        super.viewDidDisappear(animated)
    }
    func getVoice() -> String{
        if let voiceSalva = UserDefaults.standard.object(forKey: salvaVoice){
            return voiceSalva as! String
        }
        return "n"
    }
    
    func montarTabela(dados: Array<Any>){
        //print(dados[0])
        data.removeAll()
        
        var dado: InfoDadosTabelaItinerario
        for i in 0...dados.count-1 {
            let objAtual = dados[i] as? [String: Any]
            dado = InfoDadosTabelaItinerario(rua: objAtual!["rua"] as! String, numeroInicio: objAtual!["num_inicio"] as! Int, numeroFim: objAtual!["num_fim"] as! Int)
            
            data.append(dado)
            
            self.itinerario += trocaAbrev(texto: objAtual!["rua"] as! String) + " de " + String(objAtual!["num_inicio"] as! Int) + " a " + String(objAtual!["num_fim"] as! Int) + ". \n "
        }
        //self.UITableView.reloadData()
        DispatchQueue.main.async {
            self.UITableView.reloadData()
        }
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "InfoCelula") as! InfoCelula
        let obj = data[indexPath.row]
        
        cell.lblRua.text = obj.rua
        cell.lblNum?.text = String(obj.numeroInicio) + " - " + String(obj.numeroFim)
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        print(indexPath.row)
        
    }
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //Falar
    func falar(texto: String){
        myUtterance = AVSpeechUtterance(string: texto)
        myUtterance.rate = 0.45
        synth.speak(myUtterance)
    }
    
    //
    //Ouvir
    //
    func askSpeechPermission() {
        SFSpeechRecognizer.requestAuthorization { status in
            OperationQueue.main.addOperation {
                switch status {
                case .authorized:
                    self.status = .ready
                default:
                    self.status = .unavailable
                }
            }
        }
    }
    func startRecording() {
        self.comando = ""
        self.comandoAntigo = " "
        speechTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(InfoViewController.updateCounterSpeech), userInfo: nil, repeats: true)
        
        print("1")
        // Setup audio engine and speech recognizer
        let node = audioEngine.inputNode// else { return } //Tirei um guard da frente
        let recordingFormat = node.outputFormat(forBus: 0)
        node.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
            self.request.append(buffer)
        }
        print("2")
        // Prepare and start recording
        audioEngine.prepare()
        do {
            try audioEngine.start()
            self.status = .recognizing
        } catch {
            print("Errinho")
            return print(error)
        }
        print("3")
        // Analyze the speech
        recognitionTask = speechRecognizer?.recognitionTask(with: request, resultHandler: { result, error in
            if let result = result {
                print("4")
                print(result.bestTranscription.formattedString)
                self.comando = result.bestTranscription.formattedString
            } else if let error = error {
                print(error)
                print("Errinho 2")
            }
        })
    }
    func cancelRecording() {
        tocaSons(qual: 3)
        audioEngine.stop()
        let node = audioEngine.inputNode //{ //Tirei um if
        node.removeTap(onBus: 0)
        //}
        recognitionTask?.cancel()
        self.reconhecerComando()
    }
    func reconhecerComando(){
        let comand = self.comando.lowercased().trimmingCharacters(in: .whitespacesAndNewlines)
        print(comand)
        if comand == "voltar"{
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }else if comandoVoz && (comand == "horários de operação" || comand == "horário de operação"){
            self.falar(texto: self.horarios)
        }else if comandoVoz && comand == "itinerário"{
            self.falar(texto: self.itinerario)
        }
    }
    
    
    @IBAction func btnOuvirIniciar(_ sender: Any) {
        if status != .recognizing{
            print("Ouvindo")
            startRecording()
            status = .recognizing
            synth.stopSpeaking(at: AVSpeechBoundary.immediate) // Parar fala
            tocaSons(qual: 1)
        }
    }
    
    @IBAction func btnOuvirFinalizar(_ sender: Any) {
        //pararOuvir()
    }
    func pararOuvir(){
        print("Parando")
        status = .ready
        cancelRecording()
    }
    
    func tocaSons(qual: Int){
        //Inicio -> 1
        //Erro -> 2
        //Fim -> 3
        var arquivo: String = ""
        switch qual {
        case 1:
            arquivo = "inicio"
        case 2:
            arquivo = "erro"
        case 3:
            arquivo = "fim"
        default:
            arquivo = ""
        }
        if let path = Bundle.main.path(forResource: arquivo, ofType: "mp3"){
            let url = URL(fileURLWithPath: path)
            
            do{
                player = try AVAudioPlayer(contentsOf: url)
                player.delegate = self
                player.prepareToPlay()
                player.play()
            }catch{
                print("Erro ao execultar um som")
            }
        }
    }
    
    
    func trocaAbrev(texto: String) -> String{
        let s1 = texto
        var s2 = s1
        s2 = s2.replacingOccurrences(of: "VL.", with: "Vila");
        s2 = s2.replacingOccurrences(of: "JD.", with: "Jardim");
        s2 = s2.replacingOccurrences(of: "TERM.", with: "Terminal");
        s2 = s2.replacingOccurrences(of: "PRINC.", with: "Princesa");
        s2 = s2.replacingOccurrences(of: "PQ.", with: "Parque");
        s2 = s2.replacingOccurrences(of: "D.", with: "Dom");
        s2 = s2.replacingOccurrences(of: "CONJ.", with: "Conjunto");
        s2 = s2.replacingOccurrences(of: "HAB.", with: "Habitacional");
        s2 = s2.replacingOccurrences(of: "PÇA.", with: "Praça");
        s2 = s2.replacingOccurrences(of: "R.", with: "Rua");
        s2 = s2.replacingOccurrences(of: "AV.", with: "Avenida");
        s2 = s2.replacingOccurrences(of: "PTE.", with: "Ponte");
        s2 = s2.replacingOccurrences(of: "DR.", with: "Doutor");
        s2 = s2.replacingOccurrences(of: "BR.", with: "Barão");
        s2 = s2.replacingOccurrences(of: "HOSP.", with: "Hospital");
        s2 = s2.replacingOccurrences(of: "SHOP.", with: "Shopping");
        s2 = s2.replacingOccurrences(of: "CAP.", with: "Capitão");
        s2 = s2.replacingOccurrences(of: "PROF.", with: "Professor");
        s2 = s2.replacingOccurrences(of: "AL.", with: "Alameda");
        s2 = s2.replacingOccurrences(of: "CHÁC.", with: "Chácara");
        s2 = s2.replacingOccurrences(of: "PAS.", with: "Passagem");
        s2 = s2.replacingOccurrences(of: "SUB.", with: "Subterrânea");
        s2 = s2.replacingOccurrences(of: "SEN.", with: "Senador");
        s2 = s2.replacingOccurrences(of: "VIAD.", with: "Viaduto");
        s2 = s2.replacingOccurrences(of: "S.", with: "São");
        s2 = s2.replacingOccurrences(of: "STO.", with: "Santo");
        s2 = s2.replacingOccurrences(of: "STA.", with: "Santa");
        s2 = s2.replacingOccurrences(of: "NSA.", with: "Nossa");
        s2 = s2.replacingOccurrences(of: "PE.", with: "Padre");
        s2 = s2.replacingOccurrences(of: "LGO.", with: "Largo");
        s2 = s2.replacingOccurrences(of: "BRIG.", with: "Brigadeiro");
        s2 = s2.replacingOccurrences(of: "DQ.", with: "Duque");
        s2 = s2.replacingOccurrences(of: "ROD.", with: "Rodovia");
        s2 = s2.replacingOccurrences(of: "S/N.", with: "Sem nome");
        s2 = s2.replacingOccurrences(of: "CEL.", with: "Coronel");
        s2 = s2.replacingOccurrences(of: "CONS.", with: "Conselheiro");
        s2 = s2.replacingOccurrences(of: "GEN.", with: "General");
        s2 = s2.replacingOccurrences(of: "CB.", with: "Cabo");
        s2 = s2.replacingOccurrences(of: "TTE.", with: "Tenente");
        s2 = s2.replacingOccurrences(of: "MAL.", with: "Marechal");
        s2 = s2.replacingOccurrences(of: "ESTR.", with: "Estrada");
        s2 = s2.replacingOccurrences(of: "REG.", with: "Regente");
        s2 = s2.replacingOccurrences(of: "DA.", with: "Dona");
        return s2;
    }
    
}

