export interface Configuration {
    numberOfEthereumNodes: number;
    numberOfPlasmaClients: number;
    tokensPerClient: number;
    transactionsPerPlasmaBlock: number;
    plasmaChildren: number;
    numberOfPeers: number;
    blockGasLimit: number;
    transactionGas: number;
    transactionGenerationRate: number;
    numberOfTransactionGenerationIntervals: number;
}
