class BootStrap {

    def crmCoreService
    def crmAccountService
    def crmSecurityService
    def crmContactService
    def crmContentService
    def crmPluginService
    def navigationService
    def crmTaskService
    def tgmConfService

    def init = { servletContext ->

        navigationService.registerItem('main', [controller: 'crmCalendar', action: 'index', title: 'crmCalendar.index.label', order: 81])
        navigationService.updated()

        // crmContact:show << documents
        crmPluginService.registerView('crmContact', 'show', 'tabs',
                [id: "documents",
                        index: 500,
                        permission: "crmContact:show",
                        label: "crmContact.tab.documents.label",
                        template: '/crmContent/embedded',
                        plugin: "crm-content-ui",
                        model: {
                            def result = crmContentService.findResourcesByReference(crmContact)
                            return [bean: crmContact, list: result, totalCount: result.size(),
                                    reference: crmCoreService.getReferenceIdentifier(crmContact),
                                    openAction: 'show', multiple: true]
                        }]
        )

        crmPluginService.registerView('crmContact', 'show', 'tabs',
                [id: "tasks", index: 300, permission: "crmTask:show", label: "crmTask.index.label", template: '/crmTask/list', plugin: "crm-task-ui", model: {
                    def result = crmTaskService.list([reference: crmContact], [sort: 'startTime', order: 'asc'])
                    def rid = crmCoreService.getReferenceIdentifier(crmContact)
                    return [bean: crmContact, reference: rid, result: result, totalCount: result.totalCount]
                }]
        )

        def admin = crmSecurityService.createUser([username: "admin", password: "admin",
                email: "admin@thegeekmachine.co", name: "Admin", enabled: true])
		def testUser = crmSecurityService.createUser([username: "test", password: "test",
				email: "test@thegeekmachine.co", name: "Test", enabled: true])

        crmSecurityService.addPermissionAlias("permission.all", ["*:*"])

        crmSecurityService.runAs(admin.username) {
            def account = crmAccountService.createAccount([status: "active"])
            def tenant = crmSecurityService.createTenant(account, "tgm Contacts", [locale: Locale.ENGLISH])
            crmSecurityService.runAs(admin.username, tenant.id) {
                crmSecurityService.addPermissionToUser("permission.all")

                // Create some demo data.
                def type1 = crmContactService.createRelationType(name: "Organizer", param: "organizer", true)
                def type2 = crmContactService.createRelationType(name: "Owner", param: "owner", true)

                def tgmconf = crmContactService.createCompany(name: "thegeekmachine", email: "support@thegeekmachine.co",
                        address: [address1: "475 Wipro Rd, Sholinganallur", city: 'Chennai', country: "IN"], true)
                def bala = crmContactService.createPerson(firstName: "BALA", lastName: "VIGNESH",
                        email: "bala@thegeekmachine.co", title: "Awesome Organizer", true)
                crmContactService.addRelation(bala, tgmconf, type1, true, 'Awesome organizer of thegeekmachine\'s presentation')
                def google = crmContactService.createCompany(name: "Google", email: "info@google.com",
                        url: 'www.google.com', address: [city: 'Mountain View', country: "US"], true)
                def larry = crmContactService.createPerson(firstName: "Larry", lastName: "Page",
                        email: "larry@google.com", title: "Developer", true)
                crmContactService.addRelation(larry, google, type2, true)
            }
			crmSecurityService.runAs(testUser.username, tenant.id) {
				crmSecurityService.addPermissionToUser("permission.all")
			}
        }
    }

    def destroy = {
    }
}
