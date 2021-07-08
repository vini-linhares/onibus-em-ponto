//
//  ProximoMeuOnibusViewController.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 03/10/17.
//   
//

import UIKit
import AVFoundation // Falar
import Speech       //Ouvir

class ProximoMeuOnibusViewController    : UIViewController, UITableViewDataSource, AVAudioPlayerDelegate {
    
    
    
    
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
    
    
    
    
    var linha: String = ""
    var operacao: String = ""
    var sentido: String = ""
    var latlng: String = ""
    
    var endAnterior = ""
    
    var data: [ProximoMeuOnibusDadosTabela] = []
    
    var SwiftTimer = Timer()
    var buscando = false
    
    @IBOutlet weak var UITableView: UITableView!
    @IBOutlet weak var btnDireita: UIButton!
    @IBOutlet weak var lblPonto: UILabel!
    @IBOutlet weak var lblLinha: UILabel!
    
    @objc public func updateCounter() {
        if !buscando{
            buscarLinhas()
        }
    }
    
    
    
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
    var primeiraVez = true
    
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
    var textLinha = ""
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        UITableView.dataSource = self
        
        UserDefaults.standard.set("s", forKey: "Voltando")
        
        buscarLinhas()
        SwiftTimer = Timer.scheduledTimer(timeInterval: 20, target:self, selector: #selector(ProximoMeuOnibusViewController.updateCounter), userInfo: nil, repeats: true)
        
        lblPonto.text = endPonto
        lblLinha.text = textLinha
        
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
        SwiftTimer.invalidate()
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
    func buscarLinhas(){
        self.buscando = true
        //print(linha + " - " + operacao + " - " + sentido + " - " + latlng)
        if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api2/proximoBusComp?linha="+linha+"-"+operacao+"&sl="+sentido+"&ponto="+self.idPonto){
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
                                            if let array = linhas as? [Any]{
                                                if let um = array.first{
                                                    print(um)
                                                    self.montarTabela(dados: array)
                                                    self.buscando = false
                                                }
                                            }
                                        }
                                    }else if status == "erro2"{
                                        if let linhas = objetoJson["proximo_onibus"]{
                                            if let array = linhas as? [Any]{
                                                if let um = array.first{
                                                    var valores = um as? [String: Any]
                                                    self.proximaSaida = valores!["distancia_texto"] as! String
                                                    if self.proximaSaida != "-2:00"{
                                                        self.montarTabelaErro(valor: 2)
                                                        self.buscando = false
                                                    }
                                                    else{
                                                        self.montarTabelaErro(valor: 0)
                                                        self.buscando = false
                                                    }
                                                }
                                            }
                                        }
                                    }else {
                                        self.montarTabelaErro(valor: 0)
                                        self.buscando = false
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
    
    func montarTabelaErro(valor: Int){
        data.removeAll()
        var dado: ProximoMeuOnibusDadosTabela
        if valor == 0{
            dado = ProximoMeuOnibusDadosTabela(distValor: 0, distText: "", end: "Nenhum ônibus encontrado neste sentido no momento")
            if self.primeiraVez{
                if self.isVoice{
                    self.falar(texto: "Nenhum ônibus encontrado neste sentido no momento")
                }
                self.primeiraVez = false
            }
        }else if valor == 1{
            dado = ProximoMeuOnibusDadosTabela(distValor: 0, distText: "", end: "SPtrans Fora do Ar")
            if self.isVoice{
                self.falar(texto: "SPtrans Fora do Ar")
            }
        }else if valor == 2{
            dado = ProximoMeuOnibusDadosTabela(distValor: 0, distText: "", end: "Próxima saida prevista para " + proximaSaida)
        }else {
            dado = ProximoMeuOnibusDadosTabela(distValor: 0, distText: "", end: "Buscando Próximos Ônibus")
        }
        data.append(dado)
        //self.UITableView.reloadData()
        DispatchQueue.main.async {
            self.UITableView.reloadData()
            
            let index = IndexPath(item: 0, section: 0)
            let cell = self.UITableView.cellForRow(at: index) as! ProximoMeuOnibusCelula
            //cell.imgMinibus.isHidden = true
            cell.lblDist.isHidden = true
        }
    }
    
    func montarTabela(dados: Array<Any>){
        print(dados[0])
        data.removeAll()
        
        var falarOnibus = ""
        var prox = true
        
        var dado: ProximoMeuOnibusDadosTabela
        for i in 0...dados.count-1 {
            let objAtual = dados[i] as? [String: Any]
            
            let endAtual = objAtual!["rua"] as! String + ", " + (objAtual!["numero"] as! String)
            
            dado = ProximoMeuOnibusDadosTabela(distValor: objAtual!["distancia_valor"] as! Int, distText: objAtual!["distancia_texto"] as! String, end: endAtual)
            data.append(dado)
            
            if isVoice{
                if prox{
                    if endAnterior != endAtual || self.primeiraVez{
                        falarOnibus += "O próximo ônibus está a " + (objAtual!["distancia_texto"] as! String) + " na "
                        falarOnibus += (objAtual!["rua"] as! String) + ", número "
                        falarOnibus += (objAtual!["numero"] as! String) + ". \n "
                        if self.primeiraVez && dados.count > 1{
                            falarOnibus += "Encontramos mais " + String(dados.count - 1) + " Ônibus. \n "
                        }else{
                            self.primeiraVez = false
                        }
                        endAnterior = endAtual
                    }
                    prox = false
                }else if self.primeiraVez{
                    falarOnibus += "a " + (objAtual!["distancia_texto"] as! String) + " na "
                    falarOnibus += (objAtual!["rua"] as! String) + ". \n "
                }
            }
        }
        self.primeiraVez = false
        //self.UITableView.reloadData()
        DispatchQueue.main.async {
            self.UITableView.reloadData()
            let index = IndexPath(item: 0, section: 0)
            let cell = self.UITableView.cellForRow(at: index) as! ProximoMeuOnibusCelula
            cell.imgMinibus.isHidden = false
            cell.lblDist.isHidden = false
            self.UITableView.reloadData()
        }
        self.falar(texto: falarOnibus)
    }
    
    func montarTabelaNula(){
        var dado: ProximoMeuOnibusDadosTabela
        dado = ProximoMeuOnibusDadosTabela(distValor: 0 , distText: "" , end: "Nenhum ônibus encontrado neste sentido no momento" )
        data.append(dado)
        
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
        let cell = tableView.dequeueReusableCell(withIdentifier: "ProximoMeuOnibusCelula") as! ProximoMeuOnibusCelula
        let obj = data[indexPath.row]
        
        cell.lblDist?.text = obj.distText
        cell.lblEnd?.text = obj.end
        
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
        speechTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(ProximoMeuOnibusViewController.updateCounterSpeech), userInfo: nil, repeats: true)
        
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
