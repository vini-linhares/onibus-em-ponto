import UIKit
import AVFoundation // Audio
import Speech       //Ouvir

extension String {
    var westernArabicNumeralsOnly: String {
        let pattern = UnicodeScalar("0")..."9"
        return String(unicodeScalars
            .flatMap { pattern ~= $0 ? Character($0) : nil })
    }
    var byWords: [String] {
        var byWords:[String] = []
        enumerateSubstrings(in: startIndex..<endIndex, options: .byWords) {
            guard let word = $0 else { return }
            print($1,$2,$3)
            byWords.append(word)
        }
        return byWords
    }
    func firstWords(_ max: Int) -> [String] {
        return Array(byWords.prefix(max))
    }
    var firstWord: String {
        return byWords.first ?? ""
    }
    func lastWords(_ max: Int) -> [String] {
        return Array(byWords.suffix(max))
    }
    var lastWord: String {
        return byWords.last ?? ""
    }
}

class DetalhesVIewController: UIViewController, UITableViewDataSource, UITextFieldDelegate, AVAudioPlayerDelegate{
    
    
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
    
    
    
    var data: [DetalhesDadosTabela] = []
    
    @IBOutlet weak var txtLinha: UITextField!
    @IBOutlet weak var UITableView: UITableView!
    @IBOutlet weak var btnDireita: UIButton!
    
    var linhaPesquisada = false
    
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
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setVoltando(voz: false)
        
        UITableView.dataSource = self
        
        //for i in 0...1000 {
        //    data.append("\(i)")
        //}
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
            falar(texto: "Diga o número de uma linha para realizar a busca.")
        }else{
            btnDireita.isHidden = true
            isVoice = false
        }
        print("Criei")
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        synth.stopSpeaking(at: AVSpeechBoundary.immediate)
        super.viewDidDisappear(animated)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if isVoice && getVoltando() == "s"{
            setVoltando(voz: false)
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }
    }
    
    func getVoltando() -> String{
        if let voiceSalva = UserDefaults.standard.object(forKey: "Voltando"){
            return voiceSalva as! String
        }
        return "n"
    }
    func setVoltando(voz: Bool){
        if(voz){
            UserDefaults.standard.set("s", forKey: "Voltando")
        }else{
            UserDefaults.standard.set("n", forKey: "Voltando")
        }
    }
    
    func getVoice() -> String{
        if let voiceSalva = UserDefaults.standard.object(forKey: salvaVoice){
            return voiceSalva as! String
        }
        return "n"
    }
    
    func montarTabelaErro(valor: Int){
        data.removeAll()
        var dado: DetalhesDadosTabela
        var texto = ""
        if valor == 0{
            texto = "Nenhuma linha encontrada"
        }else if valor == 1{
            texto = "SPtrans fora do Ar"
        }else {
            texto = ""
        }
        dado = DetalhesDadosTabela(letreiro: "", operacao: 0, ida: texto, volta: "")
        if self.isVoice{
            self.falar(texto: texto)
        }
        data.append(dado)
        //self.UITableView.reloadData()
        DispatchQueue.main.async {
            self.UITableView.reloadData()
            
            let index = IndexPath(item: 0, section: 0)
            let cell = self.UITableView.cellForRow(at: index) as! DetalhesCelulaTableViewCell
            cell.lblLinha.isHidden = true
        }
    }
    
    func montarTabela(dados: Array<Any>){
        print(dados[0])
        data.removeAll()
        
        if isVoice{
            self.falar(texto: "Escolha uma linha, dizendo o número")
        }
        var falarLinhas = ""
        
        var dado: DetalhesDadosTabela
        for i in 0...dados.count-1 {
            let objAtual = dados[i] as? [String: Any]
            dado = DetalhesDadosTabela(letreiro: objAtual!["lt"] as! String, operacao: objAtual!["tl"] as! Int, ida: objAtual!["tp"] as! String, volta: objAtual!["ts"] as! String)
            
            data.append(dado)
            if isVoice{
                falarLinhas += " Linha " + (objAtual!["lt"] as! String) + " " + (String(objAtual!["tl"] as! Int))
                falarLinhas += " de " + (objAtual!["tp"] as! String) + " a " + (objAtual!["ts"] as! String) + ". \n "
            }
        }
        //self.UITableView.reloadData()
        DispatchQueue.main.async {
            self.UITableView.reloadData()
            
            let index = IndexPath(item: 0, section: 0)
            let cell = self.UITableView.cellForRow(at: index) as! DetalhesCelulaTableViewCell
            cell.lblLinha.isHidden = false
        }
        if isVoice{
            self.falar(texto: self.trocaAbrev(texto: falarLinhas))
        }
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "DetalhesCelulaReuso") as! DetalhesCelulaTableViewCell
        let obj = data[indexPath.row]
        
        cell.lblLinha?.text = obj.letreiro + "-" + String(obj.operacao)
        cell.lblIda?.text = obj.ida
        cell.lblVolta?.text = obj.volta
        
        return cell //4.
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        print(indexPath.row)
        
        //let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "testeV") as! ViewController
        //self.navigationController!.pushViewController(VC1, animated: true)
    }
    
    func pesquisar(texto: String){
        
                if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api/linha/?busca="+texto){
                    let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                        if erro == nil{
                            if let dadosRetorno = dados{
                                print(dadosRetorno)
                                do{
                                    if let objetoJson = try JSONSerialization.jsonObject(with: dadosRetorno, options: []) as? [String: Any]{
                                        
                                        if let linhas = objetoJson["linhas"]{
                                            
                                            //print(linhas)
                                            if let array = linhas as? [Any]{
                                                if let um = array.first{
                                                    
                                                    if let objAtual = um as? [String: Any]{
                                                        if let numLinha = objAtual["lt"] as? String{
                                                            if numLinha == "0000"{
                                                                self.montarTabelaErro(valor: 0)
                                                            }else{
                                                                self.linhaPesquisada = true
                                                                self.montarTabela(dados: array)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
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
    
    @IBAction func btnPesquisar(_ sender: Any) {
        self.txtLinha.resignFirstResponder()
        
        if let linha = txtLinha.text{
            if linha.count >= 3{
                
                self.pesquisar(texto: linha)
            }else{
                let alerta = UIAlertController(title: "Atenção", message: "Digite pelo menos 3 caracteres para relaizar a busca", preferredStyle: .alert)
                let ok = UIAlertAction(title: "OK", style: .default, handler: nil)
                alerta.addAction(ok)
                present(alerta, animated: true, completion: nil)
            }
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "InfoView"{
            if let indexPath = UITableView.indexPathForSelectedRow{
                let linhaSelecionada = self.data[indexPath.row].letreiro + "-" + String(self.data[indexPath.row].operacao)
                let vcDestino = segue.destination as! InfoViewController
                vcDestino.linha = linhaSelecionada
                print(linhaSelecionada)
            }
        }
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
        speechTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(DetalhesVIewController.updateCounterSpeech), userInfo: nil, repeats: true)
        
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
        if comand == "voltar"{
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }else{
            if !self.linhaPesquisada{
                self.pesquisar(texto: comand)
            }else{
                self.buscarEmLinhas(linha: comand)
            }
        }
    }
    
    func buscarEmLinhas(linha: String){
        var dado: DetalhesDadosTabela
        var linhaIdentificada = false
        for i in 0...self.data.count-1 {
            dado = data[i]
            var linhaComparar = dado.letreiro + " " + String(dado.operacao)
            linhaComparar = linhaComparar.replacingOccurrences(of: " ", with: "")
            linhaComparar = linhaComparar.lowercased()
            var linha2 = linha.replacingOccurrences(of: " ", with: "")
            linha2 = linha2.replacingOccurrences(of: "-", with: "")
            linha2 = linha2.replacingOccurrences(of: "/", with: "")
            linha2 = linha2.lowercased()
            print(linhaComparar)
            print(linha2)
            if ((linha2 == linhaComparar) || (corrigeFonemas(texto: trocaAcento(texto: linha2)) == corrigeFonemas(texto: trocaAcento(texto: linhaComparar)))){
                self.linhaPorFala = i
                let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "InfoView") as! InfoViewController
                
                let linha = self.data[linhaPorFala].letreiro
                let op = self.data[linhaPorFala].operacao
                let linhaSelecionada = linha! + "-" + String(describing: op!)
                VC1.linha = linhaSelecionada
                
                linhaIdentificada = true
                
                self.navigationController!.pushViewController(VC1, animated: true)
                break
            }
        }
        if !linhaIdentificada{
            for i in 0...self.data.count-1 {
                dado = data[i]
                let linhaComparar = dado.letreiro + " " + String(dado.operacao)
                print(linha.westernArabicNumeralsOnly)
                print(linhaComparar.westernArabicNumeralsOnly)
                if linha.westernArabicNumeralsOnly == linhaComparar.westernArabicNumeralsOnly{
                    self.linhaPorFala = i
                    let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "InfoView") as! InfoViewController
                    
                    let linha = self.data[linhaPorFala].letreiro
                    let op = self.data[linhaPorFala].operacao
                    let linhaSelecionada = linha! + "-" + String(describing: op!)
                    VC1.linha = linhaSelecionada
                    
                    linhaIdentificada = true
                    
                    self.navigationController!.pushViewController(VC1, animated: true)
                    break
                }
            }
        }
        if !linhaIdentificada{
            self.falar(texto: "Linha não identificada.")
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
        return s2;
    }
    func trocaAcento(texto: String) -> String{
        let s1 = texto
        var s2 = s1.lowercased()
        s2 = s2.replacingOccurrences(of: "á", with: "a")
        s2 = s2.replacingOccurrences(of: "ã", with: "a")
        s2 = s2.replacingOccurrences(of: "à", with: "a")
        s2 = s2.replacingOccurrences(of: "â", with: "a")
        s2 = s2.replacingOccurrences(of: "é", with: "e")
        s2 = s2.replacingOccurrences(of: "ê", with: "e")
        s2 = s2.replacingOccurrences(of: "í", with: "i")
        s2 = s2.replacingOccurrences(of: "î", with: "i")
        s2 = s2.replacingOccurrences(of: "ó", with: "o")
        s2 = s2.replacingOccurrences(of: "ô", with: "o")
        s2 = s2.replacingOccurrences(of: "õ", with: "o")
        s2 = s2.replacingOccurrences(of: "ú", with: "u")
        s2 = s2.replacingOccurrences(of: "û", with: "u")
        s2 = s2.replacingOccurrences(of: "ç", with: "c")
        return s2
    }
    
    func corrigeFonemas(texto: String) -> String{
        let s1 = texto
        var s2 = s1.lowercased()
        s2 = s2.replacingOccurrences(of: "el", with: "l")
        s2 = s2.replacingOccurrences(of: "se", with: "c")
        s2 = s2.replacingOccurrences(of: "car", with: "k")
        s2 = s2.replacingOccurrences(of: "e-mail", with: "m")
        return s2
    }
    
}


