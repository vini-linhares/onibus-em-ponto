//
//  ProximoSelevionarOnibusViewController.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 03/10/17.
//   
//

//
//  ProximoMeuOnibusViewController.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 03/10/17.
//   
//

import UIKit
import AVFoundation
import Speech       //Ouvir

class ProximoSelevionarOnibusViewController : UIViewController, UITableViewDataSource, AVAudioPlayerDelegate{
    
    
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
    
    
    var linhas: [ProximoSelecionarOnibusModelo] = []
    
    var linha: String = ""
    var operacao: String = ""
    var sentido: String = ""
    var latlng: String = ""
    var quantLinhas = 0
    var linhasPesquisadas = 0
    
    var data: [ProximoSelecionarOnibusDadosTabela] = []
    
    @IBOutlet weak var UITableView: UITableView!
    @IBOutlet weak var btnDireita: UIButton!
    @IBOutlet weak var lblPonto: UILabel!
    
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
    
    var idPonto = ""
    var endPonto = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        UITableView.dataSource = self
        
        UserDefaults.standard.set("s", forKey: "Voltando")
        
        for i in 0...linhas.count-1 {
            buscarLinhas(indice: i)
            //print(linhas[i].sentido)
            //print(linhas[i].linha)
            //print(linhas[i].operacao)
            //print(linhas[i].latlng)
            //print("_____________")
        }
        
        lblPonto.text = self.endPonto
        
        quantLinhas = linhas.count
        print(quantLinhas)
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
        }else{
            btnDireita.isHidden = true
            isVoice = false
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
    
    var proximaSaida = ""
    func buscarLinhas(indice: Int){
        //print(linha + " - " + operacao + " - " + sentido + " - " + latlng)
        if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api2/proximoBus?linha="+self.linhas[indice].linha+"-"+self.linhas[indice].operacao+"&sl="+self.linhas[indice].sentido+"&ponto="+self.idPonto){
            print(url)
            let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                //var backToString = String(data: dados!, encoding: String.Encoding.utf8) as String!
                //var somedata = backToString?.data(using: String.Encoding.utf8)
                //print(backToString)
                if erro == nil{
                    if let dadosRetorno = dados{
                        //print(dadosRetorno)
                        do{
                            if let objetoJson = try JSONSerialization.jsonObject(with: dadosRetorno, options: []) as? [String: Any]{
                                print(objetoJson)
                                if let status = objetoJson["status"] as? String{
                                    if status == "ok"{
                                        if let linhas = objetoJson["proximo_onibus"]{
                                            //print(linhas)
                                            if let linha = linhas as? [String: Any]{
                                                //if let um = array.first{
                                                    //print(um)
                                                    self.montarTabela(dados: linha, indice: indice)
                                                //}
                                            }
                                        }
                                    }else if status == "erro2"{
                                        print("erro 1")
                                        if let linhas = objetoJson["proximo_onibus"]{
                                            print("erro 2")
                                            if let linha = linhas as? [String: Any]{
                                                print("erro 3")
                                                self.proximaSaida = linha["distancia_texto"] as! String
                                                if self.proximaSaida != "-2:00"{
                                                    self.montarTabelaNulos(indice: indice, valor: 1)
                                                }else{
                                                    self.montarTabelaNulos(indice: indice, valor: 0)
                                                }
                                            }
                                        }
                                    }else{
                                        self.montarTabelaNulos(indice: indice, valor: 0)
                                    }
                                }
                            }
                        }catch{
                            print("Erro no Retorno")
                        }
                    }
                }else{
                    print("Erro")
                }
            }
            tarefa.resume()
        }
    }
    
    
    

    
    
    
    func montarTabela(dados: [String: Any], indice: Int){
        var dado: ProximoSelecionarOnibusDadosTabela
        
            let objAtual = dados
        dado = ProximoSelecionarOnibusDadosTabela(distValor: objAtual["distancia_valor"] as! Int, distText: objAtual["distancia_texto"] as! String, end: (objAtual["rua"] as! String + ", " + (objAtual["numero"] as! String)  ), linhaNum: self.linhas[indice].linha + "-" + self.linhas[indice].operacao, linhaNome: self.linhas[indice].nome)
        
        var adicionado = false
        if data.count > 0{
            for i in 0...data.count-1 {
                if dado.distValor < data[i].distValor{
                    data.insert(dado, at: i)
                    adicionado = true
                    break
                }else if data[i].distValor == -1{
                    data.insert(dado, at: i)
                    adicionado = true
                    break
                }
            }
        }
        if !adicionado{
            data.append(dado)
        }
        DispatchQueue.main.async {
            self.UITableView.reloadData()
        }
        self.linhasPesquisadas += 1
        print(self.linhasPesquisadas)
        if self.linhasPesquisadas >= self.quantLinhas{
            if self.isVoice{
                self.falarOnibus()
            }
        }
    }
    
    func montarTabelaNulos(indice: Int, valor: Int){
        print("erro 5")
        var dado: ProximoSelecionarOnibusDadosTabela
        if valor == 1{
            dado = ProximoSelecionarOnibusDadosTabela(distValor: -1, distText: "", end: "Próxima saida prevista para " + proximaSaida, linhaNum: self.linhas[indice].linha + "-" + self.linhas[indice].operacao, linhaNome: self.linhas[indice].nome)
        }else{
            dado = ProximoSelecionarOnibusDadosTabela(distValor: -1, distText: "", end: "Não há onibus registrado nesse sentido no momento", linhaNum: self.linhas[indice].linha + "-" + self.linhas[indice].operacao, linhaNome: self.linhas[indice].nome)
        }
        data.append(dado)
        DispatchQueue.main.async {
            self.UITableView.reloadData()
        }
        self.linhasPesquisadas += 1
        print(self.linhasPesquisadas)
        if self.linhasPesquisadas >= self.quantLinhas{
            if self.isVoice{
                self.falarOnibus()
            }
        }
    }
    
    func falarOnibus(){
        var fala = ""
        for i in 0...data.count-1 {
            if data[i].distValor != -1{
                fala += "O ônibus da linha " + data[i].linhaNum + " " + self.trocaAbrev(texto: data[i].linhaNome)
                fala += " está a " + data[i].distText + " em " + data[i].end + ". \n "
            }
        }
        self.falar(texto: fala)
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "ProximoSelecionarOnibusCelula") as! ProximoSelecionarOnibusCelula
        let obj = data[indexPath.row]
        
        cell.lblDist?.text = obj.distText
        cell.lblEnd?.text = obj.end
        cell.lblNum?.text = obj.linhaNum
        cell.lblNome?.text = obj.linhaNome
        
        return cell //4.
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //print(indexPath.row)
        
        //let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "testeV") as! ViewController
        //self.navigationController!.pushViewController(VC1, animated: true)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func trocaAbrev(texto: String) -> String{
        let s1 = texto
        var s2 = s1.uppercased()
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
        return s2;
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
        speechTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(ProximoSelevionarOnibusViewController.updateCounterSpeech), userInfo: nil, repeats: true)
        
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
        }else{
            self.falar(texto: "Comando não identificado")
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
}

