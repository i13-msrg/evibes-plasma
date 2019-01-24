export interface Configuration {
    numberOfEthereumNodes: number;
    numberOfPlasmaClients: number;
    tokensPerClient: number;
    transactionsPerPlasmaBlock: number;
    plasmaChildren: number;
    numberOfNeighbours: number;
    blockGasLimit: number;
    transactionGas: number;
    transactionGasPrice: number;
    transactionGenerationRate: number;
    numberOfTransactionGenerationIntervals: number;
    enableExternalTransactions: boolean;
    externalGasLimit: number;
    externalGasPrice: number;
    externalTransactionGenerationRate: number;
    numberOfEthereumExternalAccounts: number;
    amountPerEthereumExternalAccount: number;
    plasmaBlockInterval: number;
    neighboursUpdateInterval: number;
}
