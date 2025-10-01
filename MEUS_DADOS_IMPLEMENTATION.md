# Funcionalidade "Meus Dados" - UrbanPass

## 📋 Resumo das Implementações

Esta funcionalidade permite aos usuários visualizar seus dados pessoais e editar apenas **e-mail** e **senha**, seguindo as regras de negócio do UrbanPass.

## 🎯 Funcionalidades Implementadas

### Frontend Angular

#### 1. **Serviço de Usuário** (`user.service.ts`)
- ✅ Método `getProfile()` - busca dados do usuário logado via `/api/users/me`
- ✅ Método `updateProfile()` - atualiza apenas o e-mail do usuário
- ✅ Método `changePassword()` - altera a senha do usuário
- ✅ Método `deleteUser()` - exclui a conta do usuário
- ✅ Tratamento completo de erros com mensagens amigáveis
- ✅ Interceptação automática de token JWT

#### 2. **Componente Profile** (`profile.component.ts`)
- ✅ Interface completa para visualização de dados pessoais
- ✅ Formulário reativo para edição do e-mail
- ✅ Formulário separado para alteração de senha
- ✅ Estados de loading, error e success
- ✅ Validações de formulário (email válido, senha mínima 6 chars)
- ✅ Componente standalone (Angular 17+)

#### 3. **Interface Visual** (`profile.component.html`)
- ✅ Layout inspirado no sistema VEM mas adaptado para UrbanPass
- ✅ Navegação por abas: Dados, Endereço, Alterar Senha
- ✅ Campos não editáveis: Nome, CPF, Data nascimento, Nome da mãe
- ✅ Campo editável: E-mail (com ícone de edição)
- ✅ Seção separada para alteração de senha
- ✅ Botão de exclusão de conta com confirmação
- ✅ Alertas de feedback para usuário
- ✅ Design responsivo

#### 4. **Estilos** (`profile.component.scss`)
- ✅ Design system consistente com UrbanPass
- ✅ Cores e tipografia padronizadas
- ✅ Estados visuais (hover, focus, error, success)
- ✅ Layout responsivo para mobile
- ✅ Animações suaves e feedback visual

### Backend Java Spring Boot

#### 1. **Endpoint `/me`** (`UserController.java`)
- ✅ Novo endpoint `GET /api/users/me`
- ✅ Retorna dados do usuário autenticado
- ✅ Autenticação via JWT
- ✅ Documentação Swagger

#### 2. **Endpoints Existentes Utilizados**
- ✅ `PUT /api/users/{id}` - atualização de dados
- ✅ `PUT /api/users/{id}/password` - alteração de senha
- ✅ `DELETE /api/users/{id}` - exclusão de conta

## 🔧 Configurações Técnicas

### Segurança
- ✅ Autenticação JWT obrigatória
- ✅ Validação de usuário (só pode editar próprios dados)
- ✅ Senha atual obrigatória para alteração
- ✅ Interceptador HTTP automático para token

### Validações
- ✅ E-mail deve ter formato válido
- ✅ Senha deve ter mínimo 6 caracteres
- ✅ Campos obrigatórios validados
- ✅ Feedback visual em tempo real

### Rotas
- ✅ `/dashboard/profile` - acesso à funcionalidade
- ✅ Proteção por guard de autenticação
- ✅ Lazy loading do componente

## 📱 Como Usar

1. **Acesse** `/dashboard/profile` após fazer login
2. **Visualize** seus dados pessoais na aba "Dados"
3. **Edite** apenas o campo e-mail clicando no ícone de edição
4. **Altere** a senha na aba "Alterar senha"
5. **Exclua** a conta se necessário (com confirmação)

## 🧪 Testes

### Script de Teste Backend
```bash
# Execute o script de teste
chmod +x backend/test_user_api.sh
./backend/test_user_api.sh
```

### Teste Frontend
1. Inicie o servidor: `npm start`
2. Acesse: `http://localhost:4200/dashboard/profile`
3. Teste todas as funcionalidades

## 🔄 Fluxo de Dados

```
Frontend → UserService → HTTP Request → AuthInterceptor → Backend API
                                                            ↓
User Data ← Profile Component ← HTTP Response ← JWT Validation ← UserController
```

## 🎨 Design Guidelines

- **Cores Principais**: Azul UrbanPass (#3b82f6), Laranja (#e85d2b)
- **Typography**: Sistema de fontes padrão com pesos 400, 500, 600, 700
- **Spacing**: Sistema de espaçamento baseado em múltiplos de 4px
- **Componentes**: Botões, inputs e alertas seguem design system

## 🚀 Próximos Passos

- [ ] Implementar aba "Endereço" 
- [ ] Adicionar avatar do usuário
- [ ] Histórico de alterações
- [ ] Confirmação por e-mail para mudanças sensíveis
- [ ] Autenticação de dois fatores

## 📝 Observações Importantes

1. **Apenas e-mail editável**: Por regra de negócio, apenas o e-mail pode ser alterado na tela de dados pessoais
2. **Senha obrigatória**: Para alterar senha, sempre é necessário informar a senha atual
3. **Exclusão permanente**: A exclusão da conta é irreversível
4. **Layout inspirado**: Design inspirado no VEM mas adaptado para identidade UrbanPass
5. **Responsivo**: Interface funciona bem em desktop e mobile

---

**Desenvolvido para UrbanPass** 🚌✨