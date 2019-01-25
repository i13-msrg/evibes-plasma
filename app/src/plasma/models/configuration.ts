export interface Configuration {
    numberOfEthereumNodes: number;
    numberOfPlasmaClients: number;
    tokensPerClient: number;
    transactionsPerPlasmaBlock: number;
    plasmaChildren: number;
    numberOfNeighbours: number;
    blockGasLimit: number;
    plasmaTXGasLimit: number;
    plasmaTXGasPrice: number;
    transactionGenerationRate: number;
    numberOfTransactionGenerationIntervals: number;
    enableExternalTransactions: boolean;
    externalTxGasLimit: number;
    externalTxGasPrice: number;
    externalTransactionGenerationRate: number;
    numberOfEthereumExternalAccounts: number;
    amountPerEthereumExternalAccount: number;
    plasmaBlockInterval: number;
    neighboursUpdateInterval: number;
}
